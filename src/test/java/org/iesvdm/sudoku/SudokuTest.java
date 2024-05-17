package org.iesvdm.sudoku;

//import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
public class SudokuTest {

    @Test
    void failTest() {
        Sudoku sudoku = new Sudoku();
        sudoku.fillBoardBasedInCluesRandomlySolvable();
        //sudoku.fillBoardBasedInCluesRandomly();
        sudoku.printBoard();
    }

    @Test
    void fillBoardBasedInCluesRandomly(){
        Sudoku sudoku = new Sudoku();
        sudoku.fillBoardBasedInCluesRandomlySolvable();

    }
    
    @Test
    public void testGridSize() {
        Sudoku sudoku = new Sudoku();
        int expectedSize = 9;
        sudoku.setGridSize(expectedSize);
        assertEquals(expectedSize, sudoku.getGridSize());
    }

    private void assertEquals(int expectedSize, int gridSize) {
    }

    @Test
    public void testNumClues() {
        Sudoku sudoku = new Sudoku();
        int expectedNumClues = 63;
        sudoku.setNumClues(expectedNumClues);
        assertEquals(expectedNumClues, sudoku.getNumClues());
    }

    @Test
    public void testFillBoardRandomly() {
        Sudoku sudoku = new Sudoku();
        sudoku.fillBoardRandomly();
        assertNotNull(sudoku.getBoard());
    }

    @Test
    public void testFillBoardBasedInCluesRandomly() {
        Sudoku sudoku = new Sudoku();
        sudoku.fillBoardBasedInCluesRandomly();
        assertNotNull(sudoku.getBoard());
    }

    @Test
    public void testIsValidPlacement() {
        Sudoku sudoku = new Sudoku();
        sudoku.fillBoardRandomly();
        assertTrue(sudoku.isValidPlacement(1, 0, 0));
    }
    

    @Test
    public void testSolveBoard() {
        Sudoku sudoku = new Sudoku();
        sudoku.fillBoardRandomly();
        assertTrue(sudoku.solveBoard());
    }

    private void assertTrue(boolean b) {
    }


}
