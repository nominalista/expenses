package com.nominalista.expenses.sms

import com.nominalista.expenses.data.model.Rule
import com.nominalista.expenses.sms.MessageParser.formats
import com.nominalista.expenses.sms.MessageParser.getAmount
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MessageParserTest {

    /**
    groupSeparator=,
    decimalSeparator=.
     */
    @Test
    fun format1_() {
        // Given
        val format = formats[0]
        val rule = Rule(id = "", keywords = listOf<String>(), firstSymbol = "$", decimalSeparator = format.decimalSeparator, groupSeparator = format.groupSeparator)

        val sms = listOf(
                "Bancolombia le informa Pago por \$83,390.00 a EMPRESA DE TELECOMUN desde cta *1760. 16/03/2020 17:19. Inquietudes al 0345109095/018000931987.",
                "Bancolombia le informa Transferencia por \$297,000 desde cta *1760 a cta 29987505484. 17/03/2020 10:18. Inquietudes al 0345109095/018000931987.",
                "Bancolombia le informa Pago por \$20,000,000.00 a PAYU COLOMBIA S.A.S desde cta *1760. 11/02/2020 18:25. Inquietudes al 0345109095/018000931987.",
                "Bancolombia le informa Transferencia a cuentas de terceros por \$1,700,000.00. Inquietudes al 0345109095/018000931987.",
                "Bancolombia le informa Pago por 83,390.00\$ a EMPRESA DE TELECOMUN desde cta *1760. 16/03/2020 17:19. Inquietudes al 0345109095/018000931987.",
                "Bancolombia le informa Transferencia por 297,000\$ desde cta *1760 a cta 29987505484. 17/03/2020 10:18. Inquietudes al 0345109095/018000931987.",
                "Bancolombia le informa Pago por 20,000,000.00\$ a PAYU COLOMBIA S.A.S desde cta *1760. 11/02/2020 18:25. Inquietudes al 0345109095/018000931987."
        )

        // When
        val amounts = sms.map {
            getAmount(it, rule)
        }

        //Then
        val correct = listOf<Double>(83390.0, 297000.0, 20000000.0, 1700000.0, 83390.0, 297000.0, 20000000.0)
        amounts.zip(correct).map { (amount, correct) ->
            assertEquals(correct, amount, 0.1)
        }

    }

    /**
    groupSeparator=.
    decimalSeparator=,
     */
    @Test
    fun format2_() {
        // Given
        val format = formats[1]
        val rule = Rule(id = "", keywords = listOf<String>(), firstSymbol = "$", decimalSeparator = format.decimalSeparator, groupSeparator = format.groupSeparator)

        val sms = listOf(
                "Bancolombia le informa Compra por \$79.800,00 en MERCADOPAGO COLOMBIA 16:22. 16/03/2020 T.Cred *4253. Inquietudes al 0345109095/018000931987.",
                "Bancolombia le informa Retiro por \$600.000,00 en AUTO_PLZCE3. Hora 18:13 10/03/2020 T.Deb *6413. Inquietudes al 0345109095/018000931987.",
                "Fake por \$600.000 en AUTO_PLZCE3. Hora 18:13 10/03/2020 T.Deb *6413. Inquietudes al 0345109095/018000931987.",
                "Fake por \$1.600.000,00 en AUTO_PLZCE3. Hora 18:13 10/03/2020 T.Deb *6413. Inquietudes al 0345109095/018000931987.",
                "Bancolombia le informa Compra por 79.800,00\$ en MERCADOPAGO COLOMBIA 16:22. 16/03/2020 T.Cred *4253. Inquietudes al 0345109095/018000931987.",
                "Bancolombia le informa Retiro por 600.000,00\$ en AUTO_PLZCE3. Hora 18:13 10/03/2020 T.Deb *6413. Inquietudes al 0345109095/018000931987.",
                "Fake por 600.000\$ en AUTO_PLZCE3. Hora 18:13 10/03/2020 T.Deb *6413. Inquietudes al 0345109095/018000931987."
        )

        // When
        val amounts = sms.map {
            getAmount(it, rule)
        }

        //Then
        val correct = listOf<Double>(79800.0, 600000.0, 600000.0, 1600000.0, 79800.0, 600000.0, 600000.0)
        amounts.zip(correct).map { (amount, correct) ->
            assertEquals(correct, amount, 0.1)
        }
    }

    /**
    groupSeparator=" "
    decimalSeparator=.
     */
    @Test
    fun format3_() {
        // Given
        val format = formats[2]
        val rule = Rule(id = "", keywords = listOf<String>(), firstSymbol = "$", decimalSeparator = format.decimalSeparator, groupSeparator = format.groupSeparator)

        val sms = listOf(
                "Bancolombia le informa Pago por \$83 390.00 a EMPRESA DE TELECOMUN desde cta *1760. 16/03/2020 17:19. Inquietudes al 0345109095/018000931987.",
                "Bancolombia le informa Transferencia por \$297 000 desde cta *1760 a cta 29987505484. 17/03/2020 10:18. Inquietudes al 0345109095/018000931987.",
                "Bancolombia le informa Pago por 83 390.00\$ a EMPRESA DE TELECOMUN desde cta *1760. 16/03/2020 17:19. Inquietudes al 0345109095/018000931987.",
                "Bancolombia le informa Transferencia por 297 000\$ desde cta *1760 a cta 29987505484. 17/03/2020 10:18. Inquietudes al 0345109095/018000931987."
        )

        /**
         * Failing :c
        "Bancolombia le informa Pago por \$20 000 000.00 a PAYU COLOMBIA S.A.S desde cta *1760. 11/02/2020 18:25. Inquietudes al 0345109095/018000931987.",
        "Bancolombia le informa Transferencia a cuentas de terceros por \$1 700 000.00. Inquietudes al 0345109095/018000931987.",
        "Bancolombia le informa Pago por 20 000 000.00\$ a PAYU COLOMBIA S.A.S desde cta *1760. 11/02/2020 18:25. Inquietudes al 0345109095/018000931987."

         */

        // When
        val amounts = sms.map {
            getAmount(it, rule)
        }

        //Then
        val correct = listOf<Double>(83390.0, 297000.0, 83390.0, 297000.0)
        amounts.zip(correct).map { (amount, correct) ->
            assertEquals(correct, amount, 0.1)
        }
    }

    /**
    groupSeparator=" "
    decimalSeparator=,
     */
    @Test
    fun format4_() {
        // Given
        val format = formats[3]
        val rule = Rule(id = "", keywords = listOf<String>(), firstSymbol = "$", decimalSeparator = format.decimalSeparator, groupSeparator = format.groupSeparator)

        val sms = listOf(
                "Bancolombia le informa Pago por \$83 390,00 a EMPRESA DE TELECOMUN desde cta *1760, 16/03/2020 17:19, Inquietudes al 0345109095/018000931987,",
                "Bancolombia le informa Transferencia por \$297 000 desde cta *1760 a cta 29987505484, 17/03/2020 10:18, Inquietudes al 0345109095/018000931987,",
                "Bancolombia le informa Pago por 83 390,00\$ a EMPRESA DE TELECOMUN desde cta *1760, 16/03/2020 17:19, Inquietudes al 0345109095/018000931987,",
                "Bancolombia le informa Transferencia por 297 000\$ desde cta *1760 a cta 29987505484, 17/03/2020 10:18, Inquietudes al 0345109095/018000931987,"
        )

        /**
         * Failing :c
        "Bancolombia le informa Pago por \$20 000 000.00 a PAYU COLOMBIA S.A.S desde cta *1760. 11/02/2020 18:25. Inquietudes al 0345109095/018000931987.",
        "Bancolombia le informa Transferencia a cuentas de terceros por \$1 700 000.00. Inquietudes al 0345109095/018000931987.",
        "Bancolombia le informa Pago por 20 000 000.00\$ a PAYU COLOMBIA S.A.S desde cta *1760. 11/02/2020 18:25. Inquietudes al 0345109095/018000931987."

         */

        // When
        val amounts = sms.map {
            getAmount(it, rule)
        }

        //Then
        val correct = listOf<Double>(83390.0, 297000.0, 83390.0, 297000.0)
        amounts.zip(correct).map { (amount, correct) ->
            assertEquals(correct, amount, 0.1)
        }
    }
}
