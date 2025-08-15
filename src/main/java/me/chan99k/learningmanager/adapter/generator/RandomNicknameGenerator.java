package me.chan99k.learningmanager.adapter.generator;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import me.chan99k.learningmanager.domain.member.NicknameGenerator;

@Component
public class RandomNicknameGenerator implements NicknameGenerator {
	private static final List<String> adjectives = List.of("행복한", "똑똑한", "친절한", "나태한", "과감한", "수줍은");
	private static final List<String> animals = List.of("강아지", "고양이", "앵무새", "돼지", "코요테", "코끼리", "호랑이");
	private static final Random random = new Random();

	@Override
	public String generate() {
		String adj = adjectives.get(random.nextInt(adjectives.size()));
		String animal = animals.get(random.nextInt(animals.size()));

		return adj + animal + random.nextInt(9999) + 1;
	}
}
