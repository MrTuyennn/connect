import 'package:camera_connect/joy_stick.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class CameraJoyStick extends StatefulWidget {
  const   CameraJoyStick({super.key, required this.uuid, required this.pass});

  final String uuid;
  final String pass;

  @override
  State<CameraJoyStick> createState() => _CameraJoyStickState();
}

class _CameraJoyStickState extends State<CameraJoyStick> {
  MethodChannel? channelPlayer;

  @override
  void initState() {
    super.initState();
    channelPlayer = const MethodChannel('channelCameraPlayer');
    // channelPlayer?.invokeMethod('methodCamera', {
    //   'uuid': widget.uuid,
    //   'pass': widget.pass,
    // });
  }

  void callback(x, y) {
    print('callback x => $x and y $y');
  }

  @override
  Widget build(BuildContext context) {
    return JoyStick(
      radius: 100.0,
      stickRadius: 20,
      callback: callback,
      onPtzChanged: (value) {
        print('onPtzChanged value => $value');
        channelPlayer?.invokeMethod('methodPtz', value);
      },
    );
  }
}
