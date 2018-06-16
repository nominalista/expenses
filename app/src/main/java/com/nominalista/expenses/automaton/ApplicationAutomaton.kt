package com.nominalista.expenses.automaton

class ApplicationAutomaton
    : Automaton<ApplicationState, Any>(ApplicationState(), ApplicationMapper())