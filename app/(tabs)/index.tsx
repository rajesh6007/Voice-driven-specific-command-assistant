// App.js
import React, { useState } from "react";

import {
  StyleSheet,
  Text,
  View,
  TextInput,
  TouchableOpacity,
  ScrollView,
  Linking,
  ActivityIndicator,
} from "react-native";
import axios from "axios";

//Add to use native module
import { NativeModules, Button, Alert } from "react-native";
const { AppLauncher } = NativeModules;

type ResultItem = {
  title: string;
  link: string;
  descript?: string;
  channel?: string;
};

export default function App() {
  const [text, setText] = useState("");
  const [loading, setLoading] = useState(false);
  type ResultType = {
    error?: string;
    intent?: string;
    reply?: string;
    results?: ResultItem[];
    message?: string;
    app_name?: string;
    // Add other possible fields like `reply`, `intent`, `results`, etc.
  };

  const [result, setResult] = useState<ResultType | null>(null);
  const sendCommand = async () => {
    if (!text.trim()) return;
    setLoading(true);
    setResult(null);
    try {
      const response = await axios.post("http://192.168.18.9:8000/process", {
        text,
      });
      setResult(response.data);
    } catch (err) {
      setResult({ error: "Failed to fetch response from API." });
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>🗣️ Nepali Voice Assistant</Text>
        <Text style={styles.subtitle}>
          Enter a Nepali command (e.g. गुगलमा नेपालका प्रधानमन्त्री खोज)
        </Text>
      </View>

      <TextInput
        style={styles.textarea}
        multiline
        placeholder="तपाईंको आदेश लेख्नुहोस्..."
        value={text}
        onChangeText={setText}
      />

      <TouchableOpacity style={styles.button} onPress={sendCommand}>
        <Text style={styles.buttonText}>पठाउनुहोस्</Text>
      </TouchableOpacity>

      {/* Loading Indicator (Conditionally Rendered) */}
      {loading && ( // Renders only if 'loading' state is true
        <ActivityIndicator
          size="large"
          color="#667eea"
          style={{ marginTop: 20 }}
        />
      )}

      {/* TO open the app intent*/}
      {result?.intent === "open_app" &&
        result?.app_name && ( // Check if result is null or undefined if not then try to access app_name form the result
          <View style={styles.results}>
            <Text style={styles.reply}>
              {" "}
              The app {result.app_name} is opening
            </Text>
          </View>
        )}

      {/* Results Display (Conditionally Rendered) */}
      {result &&
        !loading && ( // Renders only if 'result' state has data and not loading
          <View style={styles.results}>
            {/* Intent Display */}

            {result.intent && ( // Renders intent if present in result
              <Text style={styles.intent}>🧠 Intent: {result.intent}</Text>
            )}

            {/* Reply Display */}
            {result.reply && <Text style={styles.reply}>{result.reply}</Text>}

            {/* OPEN APP IF INTENT IS open_app */}
            {/* Show intent feedback */}
            {result?.intent === "open_app" && result.app_name && (
              <View style={styles.results}>
                <Text style={styles.intent}>
                  📱 {result.app_name} खोलिँदैछ...
                </Text>

                <TouchableOpacity
                  style={styles.button}
                  onPress={() => {
                    try {
                      AppLauncher.openApp(result.app_name!.toLowerCase());
                    } catch (e) {
                      Alert.alert("Error", "Unable to open the app.");
                    }
                  }}
                >
                  <Text style={styles.buttonText}>
                    {result.app_name} खोल्नुहोस्
                  </Text>
                </TouchableOpacity>
              </View>
            )}

            {/* Search Results (Mapped from array) */}
            {result.results?.map(
              (
                item,
                index // Iterates over 'results' array if it exists
              ) => (
                <TouchableOpacity
                  key={index}
                  style={styles.card}
                  onPress={() => Linking.openURL(item.link)}
                >
                  <Text style={styles.linkTitle}>{item.title}</Text>
                </TouchableOpacity>
              )
            )}

            {result.message && (
              <Text style={styles.reply}>{result.message}</Text>
            )}
          </View>
        )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    backgroundColor: "#eef1f7",
    padding: 20,
  },
  header: {
    marginBottom: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: "bold",
    color: "#4b4bcb",
    textAlign: "center",
    marginBottom: 10,
  },
  subtitle: {
    fontSize: 14,
    color: "#444",
    textAlign: "center",
  },
  textarea: {
    borderWidth: 1,
    borderColor: "#ccc",
    padding: 15,
    borderRadius: 10,
    minHeight: 100,
    backgroundColor: "#fff",
    textAlignVertical: "top",
  },
  button: {
    backgroundColor: "#667eea",
    padding: 15,
    marginTop: 15,
    borderRadius: 50,
    alignItems: "center",
  },
  buttonText: {
    color: "#fff",
    fontWeight: "600",
    fontSize: 16,
  },
  results: {
    marginTop: 30,
  },
  intent: {
    fontSize: 16,
    backgroundColor: "#667eea",
    color: "#fff",
    padding: 10,
    borderRadius: 8,
    textAlign: "center",
  },
  reply: {
    fontSize: 15,
    backgroundColor: "#fff",
    padding: 10,
    marginTop: 10,
    borderRadius: 8,
    borderLeftWidth: 4,
    borderLeftColor: "#667eea",
  },
  card: {
    backgroundColor: "#fff",
    padding: 10,
    marginTop: 10,
    borderRadius: 10,
    elevation: 2,
  },
  linkTitle: {
    fontSize: 15,
    color: "#1a73e8",
    fontWeight: "500",
  },
});
