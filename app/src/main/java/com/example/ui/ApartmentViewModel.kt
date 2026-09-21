package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FlatEntity
import com.example.data.model.FlatWithPaymentStatus
import com.example.data.model.ReminderLogEntity
import com.example.data.repository.ApartmentRepository
import com.example.data.repository.AssociationSettings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class FilterStatus {
    ALL, PENDING, PAID
}

data class DashboardMetrics(
    val totalFlats: Int = 0,
    val paidCount: Int = 0,
    val pendingCount: Int = 0,
    val totalExpected: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0,
    val collectionRate: Float = 0.0f
)

class ApartmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ApartmentRepository(application)

    private val calendar = Calendar.getInstance()
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    private val _currentMonthYear = MutableStateFlow(monthFormat.format(calendar.time))
    val currentMonthYear: StateFlow<String> = _currentMonthYear.asStateFlow()

    private val _displayMonthName = MutableStateFlow(displayFormat.format(calendar.time))
    val displayMonthName: StateFlow<String> = _displayMonthName.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow(FilterStatus.ALL)
    val statusFilter: StateFlow<FilterStatus> = _statusFilter.asStateFlow()

    private val _selectedBlock = MutableStateFlow("ALL")
    val selectedBlock: StateFlow<String> = _selectedBlock.asStateFlow()

    private val _associationSettings = MutableStateFlow(repository.getSettings())
    val associationSettings: StateFlow<AssociationSettings> = _associationSettings.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val reminderLogs: StateFlow<List<ReminderLogEntity>> = repository.reminderLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Raw flats with payment status for the current selected month
    private val rawFlatsFlow = _currentMonthYear.flatMapLatest { month ->
        repository.getFlatsWithPaymentStatus(month)
    }

    // Filtered flats according to search and status filter
    val filteredFlats: StateFlow<List<FlatWithPaymentStatus>> = combine(
        rawFlatsFlow,
        _searchQuery,
        _statusFilter,
        _selectedBlock
    ) { flats, query, filter, block ->
        flats.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.flat.flatNumber.contains(query, ignoreCase = true) ||
                item.flat.residentName.contains(query, ignoreCase = true) ||
                item.flat.block.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                FilterStatus.ALL -> true
                FilterStatus.PAID -> item.isPaid
                FilterStatus.PENDING -> !item.isPaid
            }

            val matchesBlock = block == "ALL" || item.flat.block == block

            matchesQuery && matchesFilter && matchesBlock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics for the dashboard
    val metrics: StateFlow<DashboardMetrics> = rawFlatsFlow.combine(_currentMonthYear) { flats, _ ->
        val totalFlats = flats.size
        val paidFlats = flats.filter { it.isPaid }
        val pendingFlats = flats.filter { !it.isPaid }

        val totalExpected = flats.sumOf { it.effectiveAmountDue }
        val totalCollected = paidFlats.sumOf { it.payment?.amountPaid ?: it.effectiveAmountDue }
        val totalPending = pendingFlats.sumOf { it.effectiveAmountDue }
        val rate = if (totalFlats > 0) (paidFlats.size.toFloat() / totalFlats.toFloat()) else 0f

        DashboardMetrics(
            totalFlats = totalFlats,
            paidCount = paidFlats.size,
            pendingCount = pendingFlats.size,
            totalExpected = totalExpected,
            totalCollected = totalCollected,
            totalPending = totalPending,
            collectionRate = rate
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    val availableBlocks: StateFlow<List<String>> = rawFlatsFlow.combine(_selectedBlock) { flats, _ ->
        val blocks = flats.map { it.flat.block }.distinct().sorted()
        listOf("ALL") + blocks
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("ALL"))

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty(_currentMonthYear.value)
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterStatusChanged(newFilter: FilterStatus) {
        _statusFilter.value = newFilter
    }

    fun onBlockSelected(block: String) {
        _selectedBlock.value = block
    }

    fun previousMonth() {
        calendar.add(Calendar.MONTH, -1)
        updateCalendarDate()
    }

    fun nextMonth() {
        calendar.add(Calendar.MONTH, 1)
        updateCalendarDate()
    }

    fun resetToCurrentMonth() {
        calendar.time = java.util.Date()
        updateCalendarDate()
    }

    private fun updateCalendarDate() {
        _currentMonthYear.value = monthFormat.format(calendar.time)
        _displayMonthName.value = displayFormat.format(calendar.time)
    }

    fun markPaymentAsPaid(flat: FlatEntity, amount: Double, method: String) {
        viewModelScope.launch {
            repository.markAsPaid(
                flatId = flat.id,
                monthYear = _currentMonthYear.value,
                amount = amount,
                method = method,
                flatNumber = flat.flatNumber
            )
            _userMessage.emit("Payment of $${String.format("%.2f", amount)} recorded for Flat ${flat.flatNumber}")
        }
    }

    fun markPaymentAsPending(flat: FlatEntity) {
        viewModelScope.launch {
            repository.markAsPending(
                flatId = flat.id,
                monthYear = _currentMonthYear.value,
                amountDue = flat.monthlyFee
            )
            _userMessage.emit("Flat ${flat.flatNumber} payment status reset to Pending")
        }
    }

    fun sendSingleReminder(item: FlatWithPaymentStatus) {
        viewModelScope.launch {
            val delivered = repository.sendPaymentReminder(
                flat = item.flat,
                payment = item.payment,
                monthYear = _currentMonthYear.value,
                displayMonth = _displayMonthName.value
            )
            val msg = if (delivered) {
                "Payment reminder notification sent to Flat ${item.flat.flatNumber} (${item.flat.residentName})"
            } else {
                "Reminder recorded in log for Flat ${item.flat.flatNumber}"
            }
            _userMessage.emit(msg)
        }
    }

    fun sendAllPendingReminders() {
        viewModelScope.launch {
            val pendingList = filteredFlats.value.filter { !it.isPaid }
            if (pendingList.isEmpty()) {
                _userMessage.emit("No pending flats for ${_displayMonthName.value}!")
                return@launch
            }
            val count = repository.sendBatchPaymentReminders(
                pendingFlats = pendingList,
                monthYear = _currentMonthYear.value,
                displayMonth = _displayMonthName.value
            )
            _userMessage.emit("Payment reminders broadcast to $count flats for ${_displayMonthName.value}")
        }
    }

    fun addFlat(
        flatNumber: String,
        block: String,
        floor: Int,
        residentName: String,
        residentType: String,
        phone: String,
        email: String,
        monthlyFee: Double
    ) {
        viewModelScope.launch {
            val flat = FlatEntity(
                flatNumber = flatNumber,
                block = block,
                floor = floor,
                residentName = residentName,
                residentType = residentType,
                phone = phone,
                email = email,
                monthlyFee = monthlyFee
            )
            repository.addFlat(flat, _currentMonthYear.value)
            _userMessage.emit("Added flat $flatNumber ($residentName)")
        }
    }

    fun updateFlat(flat: FlatEntity) {
        viewModelScope.launch {
            repository.updateFlat(flat)
            _userMessage.emit("Updated details for Flat ${flat.flatNumber}")
        }
    }

    fun deleteFlat(flat: FlatEntity) {
        viewModelScope.launch {
            repository.deleteFlat(flat)
            _userMessage.emit("Removed Flat ${flat.flatNumber}")
        }
    }

    fun updateAssociationSettings(settings: AssociationSettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
            _associationSettings.value = settings
            _userMessage.emit("Association settings updated")
        }
    }

    fun clearReminderLogs() {
        viewModelScope.launch {
            repository.clearReminderLogs()
            _userMessage.emit("Reminder history cleared")
        }
    }
}
