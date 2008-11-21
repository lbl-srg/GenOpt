model ObjectiveFunction "Model to compute the objective function" 
  parameter String parameterFileName = "modelicaParameters.txt" 
    "File on which data is present";
  parameter String inputFileName = "modelicaSchedule.txt" 
    "File on which data is present";
  
  parameter String resultFileName = "result.txt" 
    "File on which data is present";
  parameter String header = "Objective function value" "Header for result file";
  
  parameter Modelica.SIunits.Temperature TIni(fixed=false) 
    "Initial temperature";
  
  annotation (uses(Modelica(version="2.2.1")),
    experiment(StopTime=86400, Tolerance=1e-06),
    experimentSetupOutput,
    Diagram);
  Modelica.Blocks.Math.Product product annotation (extent=[0,60; 20,80]);
  Modelica.Blocks.Sources.CombiTimeTable u(
    tableOnFile=true,
    extrapolation=Modelica.Blocks.Types.Extrapolation.HoldLastPoint,
    fileName=inputFileName,
    tableName="tab1") "Table with control input" 
    annotation (extent=[-100,-80; -80,-60]);
  Modelica.Thermal.HeatTransfer.TemperatureSensor TMea "Temperature sensor" 
    annotation (extent=[40,-40; 60,-20]);
  Modelica.Blocks.Sources.TimeTable TSet(table=[0,288.15; 25200,288.15; 25200,
        293.15; 68400,293.15; 68400,288.15; 86400,288.15]) 
    "Set point for temperature" annotation (extent=[-98,60; -78,80]);
  Modelica.Blocks.Math.Feedback feedback annotation (extent=[-42,60; -22,80]);
  Modelica.Thermal.HeatTransfer.PrescribedTemperature THea 
    "Temperature of heater" 
    annotation (extent=[-60,-40; -40,-20]);
  Modelica.Thermal.HeatTransfer.Celsius.ToKelvin toKelvin 
    annotation (extent=[-68,-80; -48,-60]);
  Modelica.Thermal.HeatTransfer.HeatCapacitor cap(
    steadyStateStart=false,
    T(start=TIni),
    C=1000000) "Heat capacity of building" 
                   annotation (extent=[18,-30; 38,-10]);
  Modelica.Thermal.HeatTransfer.ThermalConductor UAHea(G=200) 
    "UA value of heater" annotation (extent=[-20,-40; 0,-20]);
  Modelica.Thermal.HeatTransfer.ThermalConductor UABld(G=100) 
    "Average UA value of building" 
    annotation (extent=[-20,0; 0,20]);
  Modelica.Blocks.Sources.Sine sine(
    amplitude=5,
    offset=273.15,
    freqHz=1/86400,
    phase=6*3600) annotation (extent=[-100,0; -80,20]);
  Modelica.Thermal.HeatTransfer.PrescribedTemperature TOut 
    "Outside air temperature" annotation (extent=[-60,0; -40,20]);
  Modelica.Blocks.Continuous.Integrator int(initType=Modelica.Blocks.Types.Init.
        InitialState,
    y_start=0,
    k=1/86400)                   annotation (extent=[60,60; 80,80]);
  
  Modelica.Thermal.HeatTransfer.Celsius.FromKelvin fromKelvin 
    annotation (extent=[-64,30; -44,50]);
initial algorithm 
 if (resultFileName <> "") then
    Modelica.Utilities.Files.removeFile(resultFileName);
  end if;
  Modelica.Utilities.Streams.print(fileName=resultFileName, string=header);
 // Note that this assignment is done at compile time. Any changes to the file
 // after compilation have no effect.
 TIni :=Modelica.Utilities.Examples.readRealParameter(parameterFileName, "TIni");
equation 
when terminal() then
Modelica.Utilities.Streams.print("f(x) = " +
realString(number=int.y, minimumWidth=1, precision=16), resultFileName);
end when;
  connect(TSet.y, feedback.u1) 
    annotation (points=[-77,70; -40,70],
                                       style(color=74, rgbcolor={0,0,127}));
  connect(TMea.T, feedback.u2)   annotation (points=[60,-30; 80,-30; 80,40; -32,
        40; -32,62],
        style(color=74, rgbcolor={0,0,127}));
  connect(u.y[1], toKelvin.Celsius) annotation (points=[-79,-70; -70,-70],
      style(color=74, rgbcolor={0,0,127}));
  connect(toKelvin.Kelvin, THea.T)                  annotation (points=[-47,-70;
        -36,-70; -36,-52; -74,-52; -74,-30; -62,-30],
                                                    style(color=74, rgbcolor={0,
          0,127}));
  connect(cap.port, TMea.port) 
    annotation (points=[28,-30; 40,-30],
                                       style(color=42, rgbcolor={191,0,0}));
  connect(THea.port, UAHea.port_a) 
    annotation (points=[-40,-30; -20,-30],
                                         style(color=42, rgbcolor={191,0,0}));
  connect(UAHea.port_b, cap.port) 
    annotation (points=[5.55112e-16,-30; 28,-30],
                                       style(color=42, rgbcolor={191,0,0}));
  connect(UABld.port_b, cap.port) 
                               annotation (points=[5.55112e-16,10; 10,10; 10,
        -30; 28,-30],
      style(color=42, rgbcolor={191,0,0}));
  connect(TOut.port, UABld.port_a) 
    annotation (points=[-40,10; -20,10], style(color=42, rgbcolor={191,0,0}));
  connect(sine.y, TOut.T) 
    annotation (points=[-79,10; -62,10], style(color=74, rgbcolor={0,0,127}));
  connect(feedback.y, product.u2) annotation (points=[-23,70; -14,70; -14,64; 
        -2,64],              style(color=74, rgbcolor={0,0,127}));
  connect(feedback.y, product.u1) annotation (points=[-23,70; -14,70; -14,76; 
        -2,76],              style(color=74, rgbcolor={0,0,127}));
  connect(product.y, int.u) 
    annotation (points=[21,70; 58,70], style(color=74, rgbcolor={0,0,127}));
  connect(TSet.y, fromKelvin.Kelvin) annotation (points=[-77,70; -70,70; -70,40;
        -66,40], style(color=74, rgbcolor={0,0,127}));
end ObjectiveFunction;
