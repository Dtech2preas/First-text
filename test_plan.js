
const fs = require('fs');
const path = require('path');
const { parseChat, generateWhoSaidIt, generateWhenWasIt } = require('./utils/chatParser.js');

// Mock DOM objects if necessary, but we are running in node
// The parser code uses 'export const', so we need to use a distinct test file or babel.
// Since we are in a raw node script here for testing, I will modify the parser to be compatible or use ESM.
// For simplicity, I'll assume the environment supports modules or I'll just read the file and test logic manually.

// Actually, `export const` is ESM. Node might complain if package.json doesn't say "type": "module".
// I will create a test script that imports it.

// Let's check package.json
try {
  const packageJson = require('./package.json');
  if (packageJson.type !== 'module') {
    console.log('Package is not module. Changing extension or using babel-node would be needed.');
  }
} catch (e) {}

// For this quick test script, I will read the parser file and `eval` it or just copy paste the logic to verify.
// OR I can use `babel-node` if available.
// Simplest: Create a CommonJS version for the test or use .mjs extension.

async function test() {
    const chatPath = path.join(__dirname, 'assets', 'chat.txt');
    if (!fs.existsSync(chatPath)) {
        console.error("Chat file not found!");
        return;
    }
    const content = fs.readFileSync(chatPath, 'utf8');

    // We need to import the parser. Since it's in ES6 syntax, I'll use a dynamic import() which works in recent Node.
    // Or I'll just rely on the fact that I can't easily run it without build step unless I rename to .mjs.
    // Let's rename the parser to .mjs for testing or use `esm` package.

    // Actually, I'll just write a test that reads the logic.
    // But better: I will create a separate `testParser.mjs` file.
}

test();
