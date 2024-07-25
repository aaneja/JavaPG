package org.example;

public class Main
{
    public static void main(String[] args)
    {
        DuckDbBulkInsert.insertTest(20000L);
        DuckDbBulkInsert.insertTest(40000L);
        DuckDbBulkInsert.insertTest(80000L);
        DuckDbBulkInsert.insertTest(100_000L);
    }
}