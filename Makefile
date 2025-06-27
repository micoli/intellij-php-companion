readme:
	pipx run readmeai \
		--repository src/main/java \
		--api ollama \
		--badge-color A931EC \
		--badge-style flat-square \
		--header-style compact \
		--navigation-style accordion \
		--temperature 0.9 \
		--tree-max-depth 2 \
		--logo LLM \
		--emojis solar \
		-o README.md \
		--model deepseek-r1:8b
