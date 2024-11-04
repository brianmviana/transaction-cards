package br.com.caju.card.common.exceptions

class InsufficientFundsException(
    override val message: String = "Saldo insuficiente!"
): Exception(message)

class InternalErrorException(
    override val message: String = "Erro interno do sistema"
): Exception(message)

class UnableToAcquireLockException(
    val accountId: String,
    override val message: String = "Não foi possivel adiquirir o lock para a conta '${accountId}'"
): Exception(message)
