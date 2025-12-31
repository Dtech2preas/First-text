// utils/chatParser.js

// The blank name from the hexdump is a sequence of U+200E (LTR mark) and spaces or other invisible chars.
// 00000010: ... e280 8ee2 808e 20e2 ...
// e2 80 8e is U+200E (Left-to-Right Mark).
// It seems there are many of them mixed with spaces.
// Instead of matching the exact invisible string, we will match:
// 1. "lefa" -> User
// 2. Anything else that is NOT "lefa" and matches the structure -> Partner (Owami)

export const parseChat = (fileContent) => {
  const lines = fileContent.split('\n');
  const messages = [];
  const participants = new Set();

  // Regex to match: 2024/07/26, 9:31 pm - Name: Message
  // Note: The space before 'pm' in the file might be a narrow no-break space (U+202F, e2 80 af).
  // We use \s? to cover standard space or no space, and standardizing on checking for the time structure.

  // Detailed regex breakdown:
  // ^(\d{4}\/\d{2}\/\d{2})  -> Date (Group 1)
  // ,
  // (\d{1,2}:\d{2})\s?([ap]m) -> Time (Group 2: HH:MM, Group 3: am/pm)
  // \s-\s
  // (.*?):                  -> Name (Group 4)
  // \s
  // (.*)                    -> Message (Group 5)

  const regex = /^(\d{4}\/\d{2}\/\d{2}), (\d{1,2}:\d{2}).?([ap]m) - (.*?): (.*)/i;

  lines.forEach((line) => {
    // Clean invisible characters from the line for easier regex matching if needed,
    // but the name itself contains them, so we keep them for the name part.
    // However, the " - " separator is standard.

    const match = line.match(regex);
    if (match) {
      const [_, dateStr, timeStr, ampm, sender, content] = match;

      // specific logic for this couple
      // "lefa" is the user
      // The other one is the partner

      let author = 'unknown';
      if (sender.includes('lefa')) {
        author = 'lefa';
      } else {
        author = 'owami';
      }

      participants.add(author);

      // Parse date for "When was this?"
      // Date format: 2024/07/26
      // Time: 9:31 pm

      // We need a JS Date object.
      // 2024/07/26 -> 2024-07-26
      const formattedDateStr = dateStr.replace(/\//g, '-');
      // Convert 12h to 24h for safe parsing, or just let JS handle it if we format correctly.
      // Let's store components for easier quiz generation.

      messages.push({
        fullDate: dateStr,
        time: timeStr + ' ' + ampm,
        timestamp: new Date(`${formattedDateStr}T${convertTo24Hour(timeStr, ampm)}`),
        author,
        content: content.trim(),
        originalSender: sender // Debugging
      });
    } else {
        // Handle multi-line messages (append to previous)
        if (messages.length > 0) {
            messages[messages.length - 1].content += '\n' + line;
        }
    }
  });

  return { messages, participants: Array.from(participants) };
};

const convertTo24Hour = (timeStr, ampm) => {
    let [hours, minutes] = timeStr.split(':');
    hours = parseInt(hours);

    if (ampm.toLowerCase() === 'pm' && hours < 12) hours += 12;
    if (ampm.toLowerCase() === 'am' && hours === 12) hours = 0;

    return `${hours.toString().padStart(2, '0')}:${minutes}:00`;
}

// Quiz Generators
export const generateWhoSaidIt = (messages, count = 5) => {
  // Filter messages that are long enough to be interesting and not media omitted
  const candidates = messages.filter(m =>
    !m.content.includes('<Media omitted>') &&
    m.content.length > 10 &&
    !m.content.includes('Messages and calls are end-to-end encrypted')
  );

  const questions = [];
  for (let i = 0; i < count; i++) {
    if (candidates.length === 0) break;
    const randomIndex = Math.floor(Math.random() * candidates.length);
    const msg = candidates[randomIndex];

    questions.push({
      type: 'WHO_SAID_IT',
      text: msg.content,
      correctAnswer: msg.author,
      options: ['lefa', 'owami']
    });

    // Remove to avoid duplicates
    candidates.splice(randomIndex, 1);
  }
  return questions;
};

export const generateWhenWasIt = (messages, count = 5) => {
  // Filter messages
  const candidates = messages.filter(m =>
    !m.content.includes('<Media omitted>') &&
    m.content.length > 15
  );

  const questions = [];
  for (let i = 0; i < count; i++) {
     if (candidates.length === 0) break;
    const randomIndex = Math.floor(Math.random() * candidates.length);
    const msg = candidates[randomIndex];

    // Generate wrong dates (random months/years)
    const trueDate = msg.timestamp;
    const options = [trueDate];

    while (options.length < 4) {
        const randomOffset = Math.floor(Math.random() * 10000000000) * (Math.random() > 0.5 ? 1 : -1);
        const fakeDate = new Date(trueDate.getTime() + randomOffset);
        // Ensure unique options (by month/year string)
        if (!options.some(d => d.getMonth() === fakeDate.getMonth() && d.getFullYear() === fakeDate.getFullYear())) {
            options.push(fakeDate);
        }
    }

    // Shuffle options
    const shuffledOptions = options.sort(() => Math.random() - 0.5);

    questions.push({
      type: 'WHEN_WAS_IT',
      text: `"${msg.content.substring(0, 50)}..."`,
      correctAnswer: trueDate.toISOString(), // Use ISO string for comparison
      options: shuffledOptions.map(d => d.toISOString())
    });

     candidates.splice(randomIndex, 1);
  }
  return questions;
};
