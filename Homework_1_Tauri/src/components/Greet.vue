<script setup lang="ts">
import { ref } from "vue";
import { invoke } from "@tauri-apps/api/tauri";

const Msg = ref("");
const enterValue = ref("");

async function greet() {
  Msg.value = await invoke("greet", { enterValue: enterValue.value });
}
</script>

<template>
  <div class="card">
    <input
      id="greet-input"
      v-model="enterValue"
      placeholder="Enter a Decimal fraction..."
      @keyup.enter="greet"
    />
    <button type="button" @click="greet()">翻转</button>
  </div>

  <p>{{ Msg }}</p>
</template>
