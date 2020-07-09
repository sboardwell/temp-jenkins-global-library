#!/usr/bin/groovy

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def timeoutTime = config.timeoutTime ?: 24
    def proceedMessage = """Would you like to promote version ${config.version} to environment '${config.environment}'?"""

    try {
        timeout(time: timeoutTime, unit: 'HOURS') {
            echo "$proceedMessage"
        }
    } catch (err) {
        throw err
    }
}
