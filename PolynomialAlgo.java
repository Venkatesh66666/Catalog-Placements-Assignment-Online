package com.example.demo;

import org.json.JSONObject;
import java.util.*;

public class PolynomialAlgo {

    public static long convertToDecimal(String number, int base) {
        long decimalEquivalent = 0;
        long powerOfBase = 1;

        for (int index = number.length() - 1; index >= 0; --index) {
            int digit = (number.charAt(index) >= '0' && number.charAt(index) <= '9')
                    ? number.charAt(index) - '0'
                    : number.charAt(index) - 'A' + 10;

            if (digit >= base) {
                System.out.println("Invalid for base " + base + ": " + number.charAt(index));
                return -1;
            }

            decimalEquivalent += digit * powerOfBase;
            powerOfBase *= base;
        }
        return decimalEquivalent;
    }

    public static long modularInverse(long number, long modulus) {
        long initialModulus = modulus, temp1 = 0, temp2 = 1, quotient, temp;

        if (modulus == 1) return 0;

        while (number > 1) {
            quotient = number / modulus;
            temp = modulus;
            modulus = number % modulus;
            number = temp;
            temp = temp1;
            temp1 = temp2 - quotient * temp1;
            temp2 = temp;
        }

        if (temp2 < 0) temp2 += initialModulus;

        return temp2;
    }

    public static long reconstructConstantTerm(List<long[]> dataPoints, long primeNumber) {
        long constantTerm = 0;
        int requiredShares = dataPoints.size();

        for (int i = 0; i < requiredShares; ++i) {
            long xValue = dataPoints.get(i)[0];
            long yValue = dataPoints.get(i)[1];
            long numerator = 1, denominator = 1;

            for (int j = 0; j < requiredShares; ++j) {
                if (i != j) {
                    long xOther = dataPoints.get(j)[0];
                    numerator = (numerator * (primeNumber - xOther) % primeNumber) % primeNumber;
                    denominator = (denominator * (xValue - xOther + primeNumber) % primeNumber) % primeNumber;
                }
            }

            long lagrangeTerm = (yValue * numerator % primeNumber) * modularInverse(denominator, primeNumber) % primeNumber;
            constantTerm = (constantTerm + lagrangeTerm + primeNumber) % primeNumber;
        }

        return constantTerm;
    }

    public static void main(String[] args) {
        String jsonData = """
        {
            "keys": {
                "n": 10,
                "k": 7
            },
            "1": {
                "base": "6",
                "value": "13444211440455345511"
            },
            "2": {
                "base": "15",
                "value": "aed7015a346d63"
            },
            "3": {
                "base": "15",
                "value": "6aeeb69631c227c"
            },
            "4": {
                "base": "16",
                "value": "e1b5e05623d881f"
            },
            "5": {
                "base": "8",
                "value": "316034514573652620673"
            },
            "6": {
                "base": "3",
                "value": "2122212201122002221120200210011020220200"
            },
            "7": {
                "base": "3",
                "value": "20120221122211000100210021102001201112121"
            },
            "8": {
                "base": "6",
                "value": "20220554335330240002224253"
            },
            "9": {
                "base": "12",
                "value": "45153788322a1255483"
            },
            "10": {
                "base": "7",
                "value": "1101613130313526312514143"
            }
        }
        """;

        JSONObject inputObject = new JSONObject(jsonData);
        JSONObject keyDetails = inputObject.getJSONObject("keys");
        int thresholdShares = keyDetails.getInt("k");

        long primeModulus = 10007; // Example prime number
        List<long[]> validShares = new ArrayList<>();

        for (String key : inputObject.keySet()) {
            if (key.equals("keys")) continue;

            JSONObject shareDetails = inputObject.getJSONObject(key);
            int indexValue = Integer.parseInt(key);
            int baseValue = Integer.parseInt(shareDetails.getString("base"));
            String encodedValue = shareDetails.getString("value");
            long decodedValue = convertToDecimal(encodedValue, baseValue);

            if (decodedValue != -1) {
                validShares.add(new long[]{indexValue, decodedValue});
            }
        }

        if (validShares.size() >= thresholdShares) {
            long reconstructedConstant = reconstructConstantTerm(validShares, primeModulus);
            System.out.println("Reconstructed constant term (c): " + reconstructedConstant);
        } else {
            System.err.println("Not enough valid shares to reconstruct the constant term.");
        }
    }
}
