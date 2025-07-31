package com.ezt.ringify.ringtonewallpaper.screen.callscreen.service

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle

class MyConnectionService : ConnectionService() {

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        val connection = MyConnection()
        connection.setInitializing()
        connection.setActive()
        return connection
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
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
