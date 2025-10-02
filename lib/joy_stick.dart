import 'dart:math' as Math;

import 'package:camera_connect/utils/contacts.dart';
import 'package:flutter/material.dart';

class JoyStick extends StatefulWidget {
  final double radius;
  final double stickRadius;
  final Function callback;
  final ValueChanged<int>? onPtzChanged;
  const JoyStick(
      {super.key,
      required this.radius,
      required this.stickRadius,
      required this.callback,
      this.onPtzChanged
      });

  @override
  State<JoyStick> createState() => _JoyStickState();
}

class _JoyStickState extends State<JoyStick> {
  final GlobalKey _joyStickContainer = GlobalKey();
  double yOff = 0, xOff = 0;
  double _x = 0, _y = 0;

  int? _activeDirDeg;
  static const List<int> _dirs = [-90, -45, 0, 45, 90, 135, 180, -135];

  // Map từ góc (deg) => mã PTZ trong Contacts
  static const Map<int, int> _degToPtz = {
    -90: Contacts.PTZ_ACTION_UP,
    -45: Contacts.PTZ_ACTION_RIGHT_UP,
     0 : Contacts.PTZ_ACTION_RIGHT,
     45: Contacts.PTZ_ACTION_RIGHT_DOWN,
     90: Contacts.PTZ_ACTION_DOWN,
    135: Contacts.PTZ_ACTION_LEFT_DOWN,
    180: Contacts.PTZ_ACTION_LEFT,
   -135: Contacts.PTZ_ACTION_LEFT_UP,
  };

  // Ghi nhớ mã PTZ lần trước để chỉ bắn khi thay đổi
  int _lastPtz = Contacts.PTZ_STOP;

  @override
  void initState() {
    super.initState();

    WidgetsBinding.instance.addPostFrameCallback((timeStamp) {
      final RenderBox renderBoxWidget =
          _joyStickContainer.currentContext?.findRenderObject() as RenderBox;
      final offset = renderBoxWidget.localToGlobal(Offset.zero);

      xOff = offset.dx;
      yOff = offset.dy;
    });

    _centerStick();
  }

  void _centerStick() {
    setState(() {
      _x = widget.radius;
      _y = widget.radius;
    });

    _sendCoordinates(_x, _y);
  }

  int map(x, in_min, in_max, out_min, out_max) {
    return ((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min)
        .floor();
  }

  void _onPointerMove(PointerEvent details) {
    final x = details.position.dx - xOff;
    final y = details.position.dy - yOff;
    if (isStickInside(x, y, widget.radius, widget.radius,
        widget.radius - widget.stickRadius)) {
      // x and y cordinates

      setState(() {
        _x = x;
        _y = y;
      });

      _sendCoordinates(x, y);
    }
  }

  void _onPointerUp(PointerUpEvent event) {
    _centerStick();
  }

  void _sendCoordinates(double x, double y) {
   double speed = y - widget.radius;
    double direction = x - widget.radius;
    final dx = x - widget.radius;
    final dy = y - widget.radius;
    final dist = Math.sqrt(dx * dx + dy * dy);

    final maxTravel = widget.radius - widget.stickRadius;
    final isInDeadZone = dist < Math.max(4.0, maxTravel * 0.2);

    int? nextActiveDeg;
    int ptz = Contacts.PTZ_STOP;

    if (!isInDeadZone) {
      final rad = Math.atan2(dy, dx);
      final deg = rad * 180 / Math.pi;

      int best = _dirs.first;
      double bestDiff = 1e9;
      for (final c in _dirs) {
        double diff = (deg - c).abs();
        diff = Math.min(diff, 360 - diff);
        if (diff < bestDiff) {
          bestDiff = diff;
          best = c;
        }
      }
      nextActiveDeg = best;
      ptz = _degToPtz[best] ?? Contacts.PTZ_STOP;
    } else {
      nextActiveDeg = null;
      ptz = Contacts.PTZ_STOP;
    }

    if (nextActiveDeg != _activeDirDeg) {
      setState(() {
        _activeDirDeg = nextActiveDeg;
      });
    }

    if (ptz != _lastPtz) {
      _lastPtz = ptz;
      widget.onPtzChanged?.call(ptz);
    }

    var vSpeed = -1 *
        this.map(
            speed, 0, (widget.radius - widget.stickRadius).floor(), 0, 100);
    var vDirection = this.map(
        direction, 0, (widget.radius - widget.stickRadius).floor(), 0, 100);

    widget.callback(vDirection, vSpeed);
  }

  isStickInside(x, y, circleX, circleY, circleRadius) {
    var absX = Math.pow((x - circleX).abs(), 2.0);
    var absY = Math.pow((y - circleY).abs(), 2.0);
    return Math.sqrt(absX + absY) < circleRadius;
  }

  double _deg2rad(int deg) => deg * Math.pi / 180.0;

  Positioned _arrowAt(int deg, {double size = 24}) {
    final center = widget.radius;
    final r = widget.radius - size / 2 - 6;
    final rad = _deg2rad(deg);

    final x = center + r * Math.cos(rad) - size / 2;
    final y = center + r * Math.sin(rad) - size / 2;

    final rotate = rad - Math.pi / 2;

    final bool isActive = (_activeDirDeg != null && _activeDirDeg == deg);
    final Color color = isActive ? Colors.red : Colors.white;

    return Positioned(
      left: x,
      top: y,
      child: Transform.rotate(
        angle: rotate,
        child: Icon(Icons.arrow_drop_down, size: size, color: color),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Listener(
        behavior: HitTestBehavior.opaque,
        onPointerMove: _onPointerMove,
        onPointerUp: _onPointerUp,
        child: Container(
          key: _joyStickContainer,
          width: widget.radius * 2,
          height: widget.radius * 2,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(widget.radius),
            color: Colors.grey.shade800,
          ),
          child: Stack(
            children: [
              Positioned(
                left: _x - widget.stickRadius,
                top: _y - widget.stickRadius,
                child: Container(
                  width: widget.stickRadius * 2,
                  height: widget.stickRadius * 2,
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(widget.stickRadius),
                  ),
                ),
              ),
               ..._dirs.map((deg) => _arrowAt(deg)).toList(),
            ],
          ),
        ),
      ),
    );
  }
}
