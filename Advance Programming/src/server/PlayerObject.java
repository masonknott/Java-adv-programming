package server;

import java.io.PrintWriter;
import java.util.Scanner;

/*
CE303
Mason Knott - 1801459
 */

public class PlayerObject {
    // player object to be constructed with parameters
    private final int ID;
    private boolean holdsBall;
    private PrintWriter writer;
    private Scanner scanner;

    public PlayerObject(int id, boolean holdsBall, Scanner scanner, PrintWriter writer) {
        this.ID = id;
        this.holdsBall = holdsBall;
        this.writer = writer;
        this.scanner = scanner;
    }

    public int getID() {
        return ID;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public void setHoldsBall(boolean holdsBall) {
        this.holdsBall = holdsBall;
    }

    public boolean holdsBall() {
        return holdsBall;
    }

    public boolean idEquals(Object obj) {
        return ((PlayerObject) obj).getID() == getID();
    }
}
