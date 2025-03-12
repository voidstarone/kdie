package one.voidstar.kdie

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DiceCollectionTest {

    @Test
    fun testDiceCollectionInitSides() {
        val dc = DiceCollection.create(10, 20)
        assertEquals(20, dc.sides)
    }

    @Test
    fun testDiceCollectionInitCount() {
        val dc = DiceCollection.create(20, 7)
        assertEquals(20, dc.size)
    }

    @Test
    fun testDiceCollectionDieAt() {
        val dc = DiceCollection.create(20, 7)
        val die = dc.dieAt(2)
        assertNotNull(die)
    }

    @Test
    fun testDiceCollectionRoll() {
        val dc = DiceCollection.create(50, 6)

        // Roll the dice collection
        val results = dc.roll()

        // Check if we've rolled every number at least once
        val countsPerFace = IntArray(6) { 0 }

        for (roll in results) {
            countsPerFace[roll - 1]++
        }

        var haveRolledEveryNumberAtLeastOnce = true
        for (count in countsPerFace) {
            if (count == 0) {
                haveRolledEveryNumberAtLeastOnce = false
                break
            }
        }

        // With 50 dice, we should have rolled every number at least once
        assertTrue(haveRolledEveryNumberAtLeastOnce, "Expected to roll every number at least once")
    }

    @Test
    fun testDiceCollectionRollExplode() {
        val dc = DiceCollection.create(50, 6)

        // Set explosion lower bound
        dc.explosionLowerBound = 6
        assertEquals(6, dc.explosionLowerBound)

        // Roll silently
        dc.rollSilent()

        // Get last results
        val results = dc.lastResults

        // Check if we have more results than initial dice due to explosions
        assertTrue(results.size > 50, "Expected more than 50 results due to explosions")
    }

    @Test
    fun testDiceCollectionTotal() {
        val dc = DiceCollection.create(10, 6)
        dc.roll()

        // Sum of results should match the total method
        val sum = dc.lastResults.sum()
        assertEquals(sum, dc.total())
    }

    @Test
    fun testCountResultsAboveOrMatchingBound() {
        val dc = DiceCollection.create(100, 20)
        // Set predefined results for deterministic testing
        val testResults = List(100) { (it % 20) + 1 }
        dc.setResults(testResults)

        assertEquals(50, dc.countResultsAboveOrMatchingBound(11))
    }

    @Test
    fun testCountResultsBelowOrMatchingBound() {
        val dc = DiceCollection.create(100, 20)
        // Set predefined results for deterministic testing
        val testResults = List(100) { (it % 20) + 1 }
        dc.setResults(testResults)

        assertEquals(60, dc.countResultsBelowOrMatchingBound(12))
    }

    @Test
    fun testToString() {
        val dc = DiceCollection.create(3, 6)
        // Set fixed results for deterministic test
        dc.setResults(listOf(1, 2, 3))

        assertEquals("DiceCollection(6, 3){ 1, 2, 3 }", dc.toString())
    }

    @Test
    fun testExplodingDiceWithoutStacking() {
        val dc = DiceCollection.create(10, 6)
        dc.explosionLowerBound = 6
        dc.doExplosionsStack = false

        // Manually set initial roll results with some explosions
        val initialResults = List(10) { 6 } // All sixes to force explosions
        dc.setResults(initialResults)

        // Trigger explosion logic
        dc.doExplodes()

        // We should have exactly 10 additional dice (no stacking)
        assertEquals(20, dc.lastResults.size)
    }
}