package me.paolino.clusterheadachetracker

interface AuthenticationCoordinator {
    fun onAuthenticationRequired()

    fun onAuthenticationSucceeded()
}
