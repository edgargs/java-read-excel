/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.Java.ReadExcel;

import java.util.List;

/**
 *
 * @author eriosn
 */
public class TMDTemplate {
    String CAL;
    double TMD;
    List<CLTemplate> CLs;
    List<APPTemplate> CLxAPPs;
}

class CLTemplate {
    String CL;
    double TMD;
}

class APPTemplate {
    String CL;
    String APP;
    double TMD;
}
