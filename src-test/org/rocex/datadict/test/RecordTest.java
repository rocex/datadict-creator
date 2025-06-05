package org.rocex.datadict.test;

import java.time.LocalDate;

public class RecordTest
{
    Employee emp = new Employee.Builder().id("E1001").name("张三").department("IT").salary(8500.0).build();

    public record Employee(String id, String name, String department, LocalDate hireDate, double salary, boolean isFullTime)
    {
        public static class Builder
        {
            private String department = "General";
            private LocalDate hireDate = LocalDate.now();
            private String id;
            private boolean isFullTime = true;
            private String name = "Unknown";
            private double salary = 0.0;

            public Employee build()
            {
                return new Employee(id, name, department, hireDate, salary, isFullTime);
            }

            public Builder department(String department)
            {
                this.department = department;
                return this;
            }

            public Builder id(String id)
            {
                this.id = id;
                return this;
            }
            
            public Builder name(String name)
            {
                this.name = name;
                return this;
            }

            public Builder salary(double salary)
            {
                this.salary = salary;
                return this;
            }
        }
    }
}
