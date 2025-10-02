import 'package:camera_connect/camera_player.dart';
import 'package:camera_connect/joy_stick.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
      ),
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  bool showJoyStick = false;
  MethodChannel? channelPlayer;

  void callback(x, y) {
    print('callback x => $x and y $y');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Expanded(
            child: CameraPlayer(
              uuid: '82TZ1VPJMDRLS3Z4111A',
              pass: 'ZRFFHp',
              height: 700,
              width: 1000,
              mic: 1,
              sound: 1,
              type: 0,
              onPlatformViewCreated: (id) {
                channelPlayer = const MethodChannel('channelCameraPlayer');
                setState(() {
                  showJoyStick = true;
                });
              },
            ),
          ),
          if (showJoyStick) ...[
            JoyStick(
              radius: 100.0,
              stickRadius: 20,
              callback: callback,
              onPtzChanged: (value) {
                print('onPtzChanged value => $value');
                channelPlayer?.invokeMethod('methodPtz', value);
              },
            ),
            SizedBox(height: 100),
          ],
        ],
      ),
    );
  }
}
