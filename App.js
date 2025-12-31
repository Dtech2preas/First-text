import React, { useState, useEffect, useRef } from 'react';
import {
  StyleSheet, Text, View, TouchableOpacity, Animated,
  Dimensions, ImageBackground, StatusBar, SafeAreaView, ScrollView, Modal, Platform
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Audio } from 'expo-av';
import { Ionicons } from '@expo/vector-icons';
import { Asset } from 'expo-asset';
import * as FileSystem from 'expo-file-system';

import { parseChat, generateWhoSaidIt, generateWhenWasIt } from './utils/chatParser';

const { width, height } = Dimensions.get('window');

// Colors
const COLORS = {
  bg: '#0f0c29',
  bg2: '#302b63',
  bg3: '#24243e',
  accent: '#ff4b1f',
  accent2: '#ff9068',
  text: '#ffffff',
  secondaryText: '#b0b0b0',
  success: '#00b09b',
  error: '#ff416c'
};

export default function App() {
  const [screen, setScreen] = useState('LOADING'); // LOADING, INTRO, GAME, FINALE
  const [messages, setMessages] = useState([]);
  const [sound, setSound] = useState();
  const [keys, setKeys] = useState(0);
  const [stats, setStats] = useState({ total: 0, firstDate: '' });
  const KEYS_NEEDED = 5;

  // Game State
  const [currentQuestion, setCurrentQuestion] = useState(null);
  const [feedback, setFeedback] = useState(null); // 'CORRECT' | 'WRONG'
  const fadeAnim = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    loadResources();
    return () => {
      if (sound) sound.unloadAsync();
    };
  }, []);

  const loadResources = async () => {
    try {
        // Audio
        try {
            const { sound: audioSound } = await Audio.Sound.createAsync(
                require('./assets/music.mp3'),
                { isLooping: true, volume: 0.5 }
            );
            setSound(audioSound);
            await audioSound.playAsync();
        } catch (e) { console.log("Audio not available"); }

        // Chat Data
        const asset = Asset.fromModule(require('./assets/chat.txt'));
        await asset.downloadAsync();

        let text;
        if (Platform.OS === 'web') {
            const response = await fetch(asset.uri);
            text = await response.text();
        } else {
            if (asset.localUri) {
                text = await FileSystem.readAsStringAsync(asset.localUri);
            } else {
                // Fallback if localUri is missing on native (shouldn't happen with downloadAsync)
                const response = await fetch(asset.uri);
                text = await response.text();
            }
        }

        const { messages: parsedMessages } = parseChat(text);
        setMessages(parsedMessages);

        if (parsedMessages.length > 0) {
            setStats({
                total: parsedMessages.length,
                firstDate: parsedMessages[0].fullDate
            });
        }

        setTimeout(() => setScreen('INTRO'), 1000);
    } catch (e) {
        console.error("Error loading resources", e);
        setFeedback(e.message);
        setScreen('ERROR');
    }
  };

  const startGame = () => {
    setKeys(0);
    nextQuestion();
    setScreen('GAME');
  };

  const nextQuestion = () => {
    setFeedback(null);
    Animated.timing(fadeAnim, { toValue: 0, duration: 200, useNativeDriver: true }).start(() => {
        // Generate new question
        const r = Math.random();
        let q;
        if (r > 0.5) {
            q = generateWhoSaidIt(messages, 1)[0];
        } else {
            q = generateWhenWasIt(messages, 1)[0];
        }
        setCurrentQuestion(q);
        Animated.timing(fadeAnim, { toValue: 1, duration: 500, useNativeDriver: true }).start();
    });
  };

  const handleAnswer = (answer) => {
    if (!currentQuestion) return;

    let isCorrect = false;
    if (currentQuestion.type === 'WHO_SAID_IT') {
        isCorrect = answer === currentQuestion.correctAnswer;
    } else {
        // Date comparison
        // answer is ISO string, correctAnswer is ISO string
        // We only care about Month and Year for "rough" correctness or exact date?
        // The generator provides exact ISO strings.
        isCorrect = answer === currentQuestion.correctAnswer;
    }

    if (isCorrect) {
        setFeedback('CORRECT');
        setKeys(k => {
            const newKeys = k + 1;
            if (newKeys >= KEYS_NEEDED) {
                setTimeout(() => setScreen('FINALE'), 1500);
            } else {
                setTimeout(nextQuestion, 1500);
            }
            return newKeys;
        });
    } else {
        setFeedback('WRONG');
        setTimeout(nextQuestion, 1500);
    }
  };

  // RENDERERS

  if (screen === 'LOADING') {
    return (
        <View style={styles.container}>
            <Text style={{color: 'white'}}>Restoring Archive...</Text>
        </View>
    );
  }

  if (screen === 'ERROR') {
      return (
          <View style={styles.container}>
              <Text style={{color: 'red', textAlign: 'center'}}>Error Loading Archive.</Text>
              <Text style={{color: 'white', textAlign: 'center', padding: 20}}>{feedback}</Text>
              <TouchableOpacity onPress={loadResources} style={{marginTop: 20, padding: 10, backgroundColor: 'white'}}>
                  <Text>Retry</Text>
              </TouchableOpacity>
          </View>
      )
  }

  if (screen === 'INTRO') {
      return (
          <ImageBackground style={styles.container}>
              <LinearGradient colors={[COLORS.bg, COLORS.bg2]} style={styles.gradient} />
              <View style={styles.content}>
                  <Ionicons name="lock-closed-outline" size={80} color={COLORS.accent} />
                  <Text style={styles.title}>THE ARCHIVE</Text>
                  <Text style={styles.subtitle}>Corrupted Memory Detected.</Text>
                  <Text style={styles.text}>
                      {stats.total} fragments found starting from {stats.firstDate}.
                      To restore the file, you must prove your knowledge of the timeline.
                  </Text>

                  <TouchableOpacity style={styles.button} onPress={startGame}>
                      <Text style={styles.buttonText}>RESTORE DATA</Text>
                  </TouchableOpacity>
              </View>
          </ImageBackground>
      );
  }

  if (screen === 'FINALE') {
      return (
          <View style={styles.container}>
             <LinearGradient colors={[COLORS.bg, COLORS.bg3]} style={styles.gradient} />
             <ScrollView contentContainerStyle={styles.scrollContent}>
                 <Ionicons name="heart" size={100} color="#e31b23" style={{alignSelf:'center', marginBottom: 20}} />
                 <Text style={styles.title}>ACCESS GRANTED</Text>
                 <Text style={styles.subtitle}>Happy New Year, Owami</Text>

                 <View style={styles.letterContainer}>
                     <Text style={styles.letterText}>
                         {`My Dearest Owami,

If you are reading this, you have successfully unlocked our archive.
These ${stats.total} messages are just a glimpse of the story we are writing together.

Every "Hi Stranger", every joke, every moment has led us here.
You are my favorite mystery, my best friend, and my love.

Here's to another year of us.
I love you.

- Lefa`}
                     </Text>
                 </View>
                 <View style={{height: 50}} />
             </ScrollView>
          </View>
      );
  }

  // GAME SCREEN
  return (
      <View style={styles.container}>
          <LinearGradient colors={[COLORS.bg, COLORS.bg2]} style={styles.gradient} />
          <SafeAreaView style={{flex: 1, width: '100%'}}>
              <View style={styles.header}>
                  <Text style={styles.score}>KEYS: {keys} / {KEYS_NEEDED}</Text>
                  <View style={styles.progressBar}>
                      <View style={{...styles.progressFill, width: `${(keys/KEYS_NEEDED)*100}%`}} />
                  </View>
              </View>

              <Animated.View style={[styles.gameContent, { opacity: fadeAnim }]}>
                  {currentQuestion && (
                      <>
                          <Text style={styles.questionLabel}>
                              {currentQuestion.type === 'WHO_SAID_IT' ? 'WHO SAID THIS?' : 'WHEN WAS THIS SENT?'}
                          </Text>

                          <View style={styles.card}>
                              <Text style={styles.questionText}>{currentQuestion.text}</Text>
                          </View>

                          <View style={styles.optionsContainer}>
                              {currentQuestion.options.map((opt, idx) => {
                                  // Formatting for display
                                  let label = opt;
                                  if (currentQuestion.type === 'WHEN_WAS_IT') {
                                      const d = new Date(opt);
                                      label = d.toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });
                                  } else {
                                      label = opt === 'lefa' ? 'Lefa' : 'Owami';
                                  }

                                  return (
                                      <TouchableOpacity
                                        key={idx}
                                        style={styles.optionButton}
                                        onPress={() => handleAnswer(opt)}
                                      >
                                          <Text style={styles.optionText}>{label}</Text>
                                      </TouchableOpacity>
                                  );
                              })}
                          </View>
                      </>
                  )}
              </Animated.View>

              {feedback && (
                  <View style={[styles.overlay, { backgroundColor: feedback === 'CORRECT' ? 'rgba(0,176,155,0.8)' : 'rgba(255,65,108,0.8)' }]}>
                      <Text style={styles.feedbackText}>{feedback}</Text>
                  </View>
              )}
          </SafeAreaView>
      </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#000', alignItems: 'center', justifyContent: 'center' },
  gradient: { position: 'absolute', left: 0, right: 0, top: 0, bottom: 0 },
  content: { padding: 30, alignItems: 'center', width: '100%' },
  title: { fontSize: 32, fontWeight: 'bold', color: COLORS.text, marginTop: 20, letterSpacing: 5 },
  subtitle: { fontSize: 18, color: COLORS.accent, marginVertical: 10, letterSpacing: 1 },
  text: { color: COLORS.secondaryText, textAlign: 'center', marginVertical: 20, lineHeight: 24 },
  button: { backgroundColor: COLORS.accent, paddingVertical: 15, paddingHorizontal: 40, borderRadius: 30, marginTop: 20 },
  buttonText: { color: 'white', fontWeight: 'bold', fontSize: 16 },

  header: { padding: 20, width: '100%', alignItems: 'center' },
  score: { color: COLORS.accent, fontSize: 16, fontWeight: 'bold', marginBottom: 10 },
  progressBar: { height: 6, width: '100%', backgroundColor: 'rgba(255,255,255,0.2)', borderRadius: 3 },
  progressFill: { height: '100%', backgroundColor: COLORS.accent, borderRadius: 3 },

  gameContent: { flex: 1, alignItems: 'center', padding: 20, justifyContent: 'center' },
  questionLabel: { color: COLORS.secondaryText, fontSize: 14, letterSpacing: 2, marginBottom: 20 },
  card: { backgroundColor: 'rgba(255,255,255,0.1)', padding: 30, borderRadius: 20, width: '100%', marginBottom: 30 },
  questionText: { color: 'white', fontSize: 20, textAlign: 'center', fontStyle: 'italic' },

  optionsContainer: { width: '100%' },
  optionButton: { backgroundColor: 'rgba(255,255,255,0.05)', padding: 20, borderRadius: 15, marginBottom: 15, borderWidth: 1, borderColor: 'rgba(255,255,255,0.1)' },
  optionText: { color: 'white', textAlign: 'center', fontSize: 16 },

  overlay: { position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, justifyContent: 'center', alignItems: 'center' },
  feedbackText: { fontSize: 40, fontWeight: 'bold', color: 'white', letterSpacing: 3 },

  scrollContent: { padding: 30, alignItems: 'center' },
  letterContainer: { backgroundColor: 'rgba(255,255,255,0.9)', padding: 30, borderRadius: 5, marginTop: 30, width: '100%' },
  letterText: { color: '#000', fontSize: 16, lineHeight: 28, fontFamily: 'serif' }
});
