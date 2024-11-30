import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: KeyGenerationScreen(),
    );
  }
}

class KeyGenerationScreen extends StatefulWidget {
  @override
  _KeyGenerationScreenState createState() => _KeyGenerationScreenState();
}

class _KeyGenerationScreenState extends State<KeyGenerationScreen> {
  String publicKey = "Click the button to generate keys";

  static const platform = MethodChannel('com.example.keygen/keystore');

  Future<void> generateKeys() async {
    try {
      final String result = await platform.invokeMethod('generateKeys');
      setState(() {
        publicKey = result;
      });
    } on PlatformException catch (e) {
      setState(() {
        publicKey = "Failed to generate keys: ${e.message}";
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("TEE Key Generation"),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              publicKey,
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 16),
            ),
            SizedBox(height: 20),
            ElevatedButton(
              onPressed: generateKeys,
              child: Text("Generate Keys"),
            ),
          ],
        ),
      ),
    );
  }
}
