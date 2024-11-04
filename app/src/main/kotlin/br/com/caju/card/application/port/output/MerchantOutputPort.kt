package br.com.caju.card.application.port.output

interface MerchantOutputPort {
    fun findMerchantByName(name: String): MerchantData?
}

data class MerchantData(
    val name: String,
    val mcc: Int,
)