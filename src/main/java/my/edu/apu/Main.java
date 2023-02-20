package my.edu.apu;

import my.edu.apu.shared.AirplaneState;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        AirplaneState state = new AirplaneState(400, 0, 0, false, false);
        byte[] stateBytes = state.getBytes();
        System.out.println(Arrays.toString(stateBytes));
        System.out.println("Hello world!");
        AirplaneState as = AirplaneState.fromBytes(stateBytes);
        System.out.println(as);
    }
}