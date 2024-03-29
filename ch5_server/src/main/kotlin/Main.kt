fun main() {
    listOf(
        SingleThreadServer(8080),
        ThreadPerConnectionServer(8081),
        ConnectionPoolServer(8082),
        HttpTcpNettyServer(8083),
        EurUsdCurrencyRxTcpServer(8084),
        EurUsdCurrencyRxRestServer(8085),
    ).forEach {
        println("${it.javaClass.simpleName} started...")
        it.start()
    }
}
