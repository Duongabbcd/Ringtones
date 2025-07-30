package com.ezt.ringify.ringtonewallpaper.screen.callscreen

import android.telecom.Connection
import android.telecom.ConnectionService

class MyConnectionService : ConnectionService() {

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: android.telecom.PhoneAccountHandle,
        request: android.telecom.ConnectionRequest
    ): Connection {
        val connection = MyConnection()
        connection.setInitializing()
        connection.setActive()
        return connection
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: android.telecom.PhoneAccountHandle,
        request: android.telecom.ConnectionRequest
    ): Connection {
        val connection = MyConnection()
        connection.setInitializing()
        connection.setActive()
        return connection
    }
}

class MyConnection : Connection() {
    // Override methods as needed for call states/actions
}
