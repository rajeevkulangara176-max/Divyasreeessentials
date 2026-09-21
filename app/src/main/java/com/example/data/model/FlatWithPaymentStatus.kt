package com.example.data.model

data class FlatWithPaymentStatus(
    val flat: FlatEntity,
    val payment: PaymentEntity?
) {
    val isPaid: Boolean get() = payment?.status == "PAID"
    val effectiveAmountDue: Double get() = payment?.amountDue ?: flat.monthlyFee
    val status: String get() = payment?.status ?: "PENDING"
}
