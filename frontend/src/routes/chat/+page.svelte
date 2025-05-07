<script>
  import { page } from '$app/stores';
  // Statt $state verwenden wir die normale Svelte-Reaktivität
  
  const api_root = page.url.origin;
  
  let message = '';
  let messages = [
    { type: "bot", text: "Hallo! Wie kann ich dir helfen?" },
  ];
  let isLoading = false;

  async function sendMessage() {
    if (!message || isLoading) return;
    
    // Nachricht des Benutzers hinzufügen
    messages = [...messages, { type: "user", text: message }];
    const sentMessage = message;
    message = ''; // Eingabefeld zurücksetzen
    isLoading = true;
    
    try {
      // API-Aufruf an den Chatbot
      const response = await fetch(`${api_root}/api/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message: sentMessage }),
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      // Antwort des Bots hinzufügen
      messages = [...messages, { type: "bot", text: data.reply }];
    } catch (error) {
      console.error('Error sending message:', error);
      messages = [...messages, { type: "bot", text: "Es ist ein Fehler aufgetreten. Bitte versuche es später erneut." }];
    } finally {
      isLoading = false;
    }
  }

  function handleKeyDown(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      sendMessage();
    }
  }
</script>

<div class="chat-container">
  <div class="messages">
    {#each messages as message}
      <div class="message {message.type}">
        <div class="message-content">{message.text}</div>
      </div>
    {/each}
    {#if isLoading}
      <div class="message bot">
        <div class="message-content loading">...</div>
      </div>
    {/if}
  </div>
  
  <div class="input-container">
    <textarea 
      bind:value={message} 
      on:keydown={handleKeyDown}
      placeholder="Nachricht eingeben..."
      rows="1"
    ></textarea>
    <button on:click={sendMessage} disabled={!message || isLoading}>
      Senden
    </button>
  </div>
</div>

<style>
  .chat-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    max-width: 800px;
    margin: 0 auto;
  }
  
  .messages {
    flex-grow: 1;
    overflow-y: auto;
    padding: 1rem;
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .message {
    max-width: 80%;
    padding: 0.75rem 1rem;
    border-radius: 1rem;
    word-break: break-word;
  }
  
  .message.user {
    align-self: flex-end;
    background-color: #007bff;
    color: white;
    border-bottom-right-radius: 0.25rem;
  }
  
  .message.bot {
    align-self: flex-start;
    background-color: #f1f1f1;
    color: #333;
    border-bottom-left-radius: 0.25rem;
  }
  
  .loading {
    animation: pulse 1.5s infinite;
  }
  
  @keyframes pulse {
    0% { opacity: 0.5; }
    50% { opacity: 1; }
    100% { opacity: 0.5; }
  }
  
  .input-container {
    display: flex;
    padding: 1rem;
    gap: 0.5rem;
    border-top: 1px solid #eee;
  }
  
  textarea {
    flex-grow: 1;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 0.5rem;
    resize: none;
    font-family: inherit;
  }
  
  button {
    padding: 0.75rem 1.5rem;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 0.5rem;
    cursor: pointer;
    font-weight: bold;
  }
  
  button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
  }
</style>