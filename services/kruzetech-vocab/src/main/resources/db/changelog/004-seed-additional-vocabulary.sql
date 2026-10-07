-- ============================================================================
-- SEED DATA: 5 Additional System Decks, System Tags, Multi-Dimensional Vocabulary & Multi-Level Exercises
-- Level 2: Sentence Builder & Level 3: Fill-in-the-Blank Typing
-- ============================================================================

-- 1. Insert 5 New System Decks
INSERT INTO decks (id, user_id, name, description, category, icon_url, cefr_level, created_at)
VALUES ('11111111-0000-0000-0000-000000000006', 'system', 'Business, Finance & Global Economy', 'Tài chính vĩ mô, lạm phát, thị trường vốn, quản trị rủi ro & thương mại quốc tế', 'topic', '📈', 'C1-C2', NOW())
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  category = EXCLUDED.category,
  icon_url = EXCLUDED.icon_url,
  cefr_level = EXCLUDED.cefr_level;

INSERT INTO decks (id, user_id, name, description, category, icon_url, cefr_level, created_at)
VALUES ('11111111-0000-0000-0000-000000000007', 'system', 'Medicine, Healthcare & Epidemiology', 'Dược học, dịch tễ học, đột biến sinh học, phác đồ điều trị & y tế cộng đồng', 'topic', '🩺', 'B2-C2', NOW())
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  category = EXCLUDED.category,
  icon_url = EXCLUDED.icon_url,
  cefr_level = EXCLUDED.cefr_level;

INSERT INTO decks (id, user_id, name, description, category, icon_url, cefr_level, created_at)
VALUES ('11111111-0000-0000-0000-000000000008', 'system', 'Psychology, Behavior & Human Mind', 'Nhận thức, tâm lý học hành vi, cảm xúc, định kiến nhận thức & sức khỏe tinh thần', 'topic', '🧠', 'C1-C2', NOW())
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  category = EXCLUDED.category,
  icon_url = EXCLUDED.icon_url,
  cefr_level = EXCLUDED.cefr_level;

INSERT INTO decks (id, user_id, name, description, category, icon_url, cefr_level, created_at)
VALUES ('11111111-0000-0000-0000-000000000009', 'system', 'Urbanization, Smart Cities & Architecture', 'Đô thị hóa, cơ sở hạ tầng bền vững, giao thông công cộng & quy hoạch không gian', 'topic', '🏙️', 'B2-C1', NOW())
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  category = EXCLUDED.category,
  icon_url = EXCLUDED.icon_url,
  cefr_level = EXCLUDED.cefr_level;

INSERT INTO decks (id, user_id, name, description, category, icon_url, cefr_level, created_at)
VALUES ('11111111-0000-0000-0000-000000000010', 'system', 'Education, Pedagogy & Critical Thinking', 'Phương pháp sư phạm, đổi mới giáo dục, tư duy phản biện & năng lực học tập suốt đời', 'topic', '🎓', 'B2-C2', NOW())
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  category = EXCLUDED.category,
  icon_url = EXCLUDED.icon_url,
  cefr_level = EXCLUDED.cefr_level;

-- 2. Insert System Tags
INSERT INTO tags (id, user_id, name, color, created_at)
VALUES ('tag-business', NULL, 'business_finance', '#059669', NOW())
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, color = EXCLUDED.color;

INSERT INTO tags (id, user_id, name, color, created_at)
VALUES ('tag-medicine', NULL, 'medicine_health', '#DC2626', NOW())
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, color = EXCLUDED.color;

INSERT INTO tags (id, user_id, name, color, created_at)
VALUES ('tag-psychology', NULL, 'psychology_mind', '#D97706', NOW())
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, color = EXCLUDED.color;

INSERT INTO tags (id, user_id, name, color, created_at)
VALUES ('tag-urban', NULL, 'urban_architecture', '#0284C7', NOW())
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, color = EXCLUDED.color;

INSERT INTO tags (id, user_id, name, color, created_at)
VALUES ('tag-education', NULL, 'education_pedagogy', '#4F46E5', NOW())
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, color = EXCLUDED.color;

-- ============================================================================
-- 3. VOCABULARY CARDS & EXERCISES
-- ============================================================================

-- DECK 6: Business, Finance & Global Economy
-- Card 1: fiscal
INSERT INTO vocab_cards (id, deck_id, term, phonetic, cefr_level, frequency_rank, meanings, collocations, created_at)
VALUES ('c0000006-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000006', 'fiscal', '/ˈfɪs.kəl/', 'C1', 1, '[{"order": 1, "pos": "a", "meaning_vi": "thuộc về tài khóa, ngân sách nhà nước", "definition_en": "Connected with public money, especially taxes and government spending", "example_en": "The government introduced stringent fiscal stimulus measures to revitalize the national economy.", "example_vi": "Chính phủ đã đưa ra các biện pháp kích thích tài khóa nghiêm ngặt để hồi sinh nền kinh tế quốc gia."}]'::jsonb, '["fiscal policy", "fiscal deficit", "fiscal austerity", "fiscal stimulus"]'::jsonb, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000006-0000-0000-0000-000000000001', 'c0000006-0000-0000-0000-000000000001', 'sentence_builder', 'thuộc về tài khóa, ngân sách nhà nước', 'The government introduced stringent fiscal stimulus measures to revitalize the national economy.', 'Chính phủ đã đưa ra các biện pháp kích thích tài khóa nghiêm ngặt để hồi sinh nền kinh tế quốc gia.', '["The", "government", "introduced", "stringent", "fiscal", "stimulus", "measures", "to", "revitalize", "the", "national", "economy", "."]'::jsonb, '["negligent", "fragile"]'::jsonb, 4, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000006-0000-0000-0000-000000000002', 'c0000006-0000-0000-0000-000000000001', 'typing', 'thuộc về tài khóa, ngân sách nhà nước', 'The government introduced stringent ______ stimulus measures to revitalize the national economy.', 'Chính phủ đã đưa ra các biện pháp kích thích tài khóa nghiêm ngặt để hồi sinh nền kinh tế quốc gia.', '["fiscal"]'::jsonb, '["negligent", "fragile"]'::jsonb, 0, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000006-0000-0000-0000-000000000001', 'tag-c1') ON CONFLICT DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000006-0000-0000-0000-000000000001', 'tag-business') ON CONFLICT DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000006-0000-0000-0000-000000000001', 'tag-academic') ON CONFLICT DO NOTHING;

-- Card 2: liquidity
INSERT INTO vocab_cards (id, deck_id, term, phonetic, cefr_level, frequency_rank, meanings, collocations, created_at)
VALUES ('c0000006-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000006', 'liquidity', '/lɪˈkwɪd.ə.ti/', 'C1', 2, '[{"order": 1, "pos": "n", "meaning_vi": "tính thanh khoản, khả năng chuyển đổi thành tiền mặt", "definition_en": "The state of having enough cash or assets that can be easily converted into cash", "example_en": "Commercial banks faced a severe liquidity shortage during the unprecedented financial crisis.", "example_vi": "Các ngân hàng thương mại đã phải đối mặt với tình trạng thiếu hụt thanh khoản nghiêm trọng trong cuộc khủng hoảng tài chính chưa từng có."}]'::jsonb, '["market liquidity", "liquidity crisis", "provide liquidity", "dry up"]'::jsonb, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000006-0000-0000-0000-000000000003', 'c0000006-0000-0000-0000-000000000002', 'sentence_builder', 'tính thanh khoản', 'Commercial banks faced a severe liquidity shortage during the unprecedented financial crisis.', 'Các ngân hàng thương mại đã phải đối mặt với tình trạng thiếu hụt thanh khoản nghiêm trọng trong cuộc khủng hoảng tài chính chưa từng có.', '["Commercial", "banks", "faced", "a", "severe", "liquidity", "shortage", "during", "the", "unprecedented", "financial", "crisis", "."]'::jsonb, '["abundance", "volatility"]'::jsonb, 5, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000006-0000-0000-0000-000000000004', 'c0000006-0000-0000-0000-000000000002', 'typing', 'tính thanh khoản', 'Commercial banks faced a severe ______ shortage during the unprecedented financial crisis.', 'Các ngân hàng thương mại đã phải đối mặt với tình trạng thiếu hụt thanh khoản nghiêm trọng trong cuộc khủng hoảng tài chính chưa từng có.', '["liquidity"]'::jsonb, '["abundance", "volatility"]'::jsonb, 0, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000006-0000-0000-0000-000000000002', 'tag-c1') ON CONFLICT DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000006-0000-0000-0000-000000000002', 'tag-business') ON CONFLICT DO NOTHING;

-- Card 3: volatile
INSERT INTO vocab_cards (id, deck_id, term, phonetic, cefr_level, frequency_rank, meanings, collocations, created_at)
VALUES ('c0000006-0000-0000-0000-000000000003', '11111111-0000-0000-0000-000000000006', 'volatile', '/ˈvɒl.ə.taɪl/', 'C1', 3, '[{"order": 1, "pos": "a", "meaning_vi": "biến động mạnh, không ổn định (thị trường)", "definition_en": "Likely to change suddenly and unexpectedly, especially by getting worse", "example_en": "Investors must exercise extreme caution when navigating an increasingly volatile equity market.", "example_vi": "Các nhà đầu tư phải hết sức thận trọng khi định hướng trên một thị trường chứng khoán ngày càng biến động mạnh."}]'::jsonb, '["volatile market", "highly volatile", "volatile commodity prices"]'::jsonb, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000006-0000-0000-0000-000000000005', 'c0000006-0000-0000-0000-000000000003', 'sentence_builder', 'biến động mạnh, không ổn định', 'Investors must exercise extreme caution when navigating an increasingly volatile equity market.', 'Các nhà đầu tư phải hết sức thận trọng khi định hướng trên một thị trường chứng khoán ngày càng biến động mạnh.', '["Investors", "must", "exercise", "extreme", "caution", "when", "navigating", "an", "increasingly", "volatile", "equity", "market", "."]'::jsonb, '["stagnant", "immutable"]'::jsonb, 9, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000006-0000-0000-0000-000000000006', 'c0000006-0000-0000-0000-000000000003', 'typing', 'biến động mạnh, không ổn định', 'Investors must exercise extreme caution when navigating an increasingly ______ equity market.', 'Các nhà đầu tư phải hết sức thận trọng khi định hướng trên một thị trường chứng khoán ngày càng biến động mạnh.', '["volatile"]'::jsonb, '["stagnant", "immutable"]'::jsonb, 0, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000006-0000-0000-0000-000000000003', 'tag-c1') ON CONFLICT DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000006-0000-0000-0000-000000000003', 'tag-business') ON CONFLICT DO NOTHING;

-- DECK 7: Medicine, Healthcare & Epidemiology
-- Card 4: efficacy
INSERT INTO vocab_cards (id, deck_id, term, phonetic, cefr_level, frequency_rank, meanings, collocations, created_at)
VALUES ('c0000007-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000007', 'efficacy', '/ˈef.ɪ.kə.si/', 'C2', 1, '[{"order": 1, "pos": "n", "meaning_vi": "hiệu lực, hiệu quả điều trị của thuốc hoặc phác đồ", "definition_en": "The ability, especially of a medicine or a method of achieving something, to produce the intended result", "example_en": "Extensive clinical trials confirmed the superior efficacy of the innovative vaccine candidate.", "example_vi": "Các thử nghiệm lâm sàng diện rộng đã xác nhận hiệu lực vượt trội của ứng viên vắc-xin tiên tiến này."}]'::jsonb, '["demonstrate efficacy", "vaccine efficacy", "therapeutic efficacy", "clinical efficacy"]'::jsonb, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000007-0000-0000-0000-000000000001', 'c0000007-0000-0000-0000-000000000001', 'sentence_builder', 'hiệu lực, hiệu quả điều trị', 'Extensive clinical trials confirmed the superior efficacy of the innovative vaccine candidate.', 'Các thử nghiệm lâm sàng diện rộng đã xác nhận hiệu lực vượt trội của ứng viên vắc-xin tiên tiến này.', '["Extensive", "clinical", "trials", "confirmed", "the", "superior", "efficacy", "of", "the", "innovative", "vaccine", "candidate", "."]'::jsonb, '["toxicity", "deficiency"]'::jsonb, 6, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000007-0000-0000-0000-000000000002', 'c0000007-0000-0000-0000-000000000001', 'typing', 'hiệu lực, hiệu quả điều trị', 'Extensive clinical trials confirmed the superior ______ of the innovative vaccine candidate.', 'Các thử nghiệm lâm sàng diện rộng đã xác nhận hiệu lực vượt trội của ứng viên vắc-xin tiên tiến này.', '["efficacy"]'::jsonb, '["toxicity", "deficiency"]'::jsonb, 0, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000007-0000-0000-0000-000000000001', 'tag-c2') ON CONFLICT DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000007-0000-0000-0000-000000000001', 'tag-medicine') ON CONFLICT DO NOTHING;

-- Card 5: virulent
INSERT INTO vocab_cards (id, deck_id, term, phonetic, cefr_level, frequency_rank, meanings, collocations, created_at)
VALUES ('c0000007-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000007', 'virulent', '/ˈvɪr.jə.lənt/', 'C2', 2, '[{"order": 1, "pos": "a", "meaning_vi": "độc lực mạnh, lây lan và gây bệnh trầm trọng", "definition_en": "Extremely severe or harmful in its effects (of a disease or poison)", "example_en": "Epidemiologists warned that the newly mutated pathogen was significantly more virulent than previous strains.", "example_vi": "Các nhà dịch tễ học cảnh báo rằng mầm bệnh đột biến mới có độc lực mạnh hơn đáng kể so với các chủng trước đây."}]'::jsonb, '["virulent pathogen", "virulent strain", "virulent infection"]'::jsonb, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000007-0000-0000-0000-000000000003', 'c0000007-0000-0000-0000-000000000002', 'sentence_builder', 'độc lực mạnh, gây bệnh trầm trọng', 'Epidemiologists warned that the newly mutated pathogen was significantly more virulent than previous strains.', 'Các nhà dịch tễ học cảnh báo rằng mầm bệnh đột biến mới có độc lực mạnh hơn đáng kể so với các chủng trước đây.', '["Epidemiologists", "warned", "that", "the", "newly", "mutated", "pathogen", "was", "significantly", "more", "virulent", "than", "previous", "strains", "."]'::jsonb, '["benign", "dormant"]'::jsonb, 10, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000007-0000-0000-0000-000000000004', 'c0000007-0000-0000-0000-000000000002', 'typing', 'độc lực mạnh, gây bệnh trầm trọng', 'Epidemiologists warned that the newly mutated pathogen was significantly more ______ than previous strains.', 'Các nhà dịch tễ học cảnh báo rằng mầm bệnh đột biến mới có độc lực mạnh hơn đáng kể so với các chủng trước đây.', '["virulent"]'::jsonb, '["benign", "dormant"]'::jsonb, 0, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000007-0000-0000-0000-000000000002', 'tag-c2') ON CONFLICT DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000007-0000-0000-0000-000000000002', 'tag-medicine') ON CONFLICT DO NOTHING;

-- DECK 8: Psychology, Behavior & Human Mind
-- Card 6: cognitive
INSERT INTO vocab_cards (id, deck_id, term, phonetic, cefr_level, frequency_rank, meanings, collocations, created_at)
VALUES ('c0000008-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000008', 'cognitive', '/ˈkɒɡ.nə.tɪv/', 'C1', 1, '[{"order": 1, "pos": "a", "meaning_vi": "thuộc về nhận thức, quá trình tư duy não bộ", "definition_en": "Connected with thinking or conscious mental processes", "example_en": "Chronic sleep deprivation significantly impairs cognitive performance and emotional regulation.", "example_vi": "Tình trạng thiếu ngủ kinh niên làm suy giảm đáng kể hiệu suất nhận thức và khả năng điều tiết cảm xúc."}]'::jsonb, '["cognitive dissonance", "cognitive bias", "cognitive impairment", "cognitive development"]'::jsonb, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000008-0000-0000-0000-000000000001', 'c0000008-0000-0000-0000-000000000001', 'sentence_builder', 'thuộc về nhận thức, tư duy não bộ', 'Chronic sleep deprivation significantly impairs cognitive performance and emotional regulation.', 'Tình trạng thiếu ngủ kinh niên làm suy giảm đáng kể hiệu suất nhận thức và khả năng điều tiết cảm xúc.', '["Chronic", "sleep", "deprivation", "significantly", "impairs", "cognitive", "performance", "and", "emotional", "regulation", "."]'::jsonb, '["sensory", "muscular"]'::jsonb, 5, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000008-0000-0000-0000-000000000002', 'c0000008-0000-0000-0000-000000000001', 'typing', 'thuộc về nhận thức, tư duy não bộ', 'Chronic sleep deprivation significantly impairs ______ performance and emotional regulation.', 'Tình trạng thiếu ngủ kinh niên làm suy giảm đáng kể hiệu suất nhận thức và khả năng điều tiết cảm xúc.', '["cognitive"]'::jsonb, '["sensory", "muscular"]'::jsonb, 0, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000008-0000-0000-0000-000000000001', 'tag-c1') ON CONFLICT DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000008-0000-0000-0000-000000000001', 'tag-psychology') ON CONFLICT DO NOTHING;

-- Card 7: predisposition
INSERT INTO vocab_cards (id, deck_id, term, phonetic, cefr_level, frequency_rank, meanings, collocations, created_at)
VALUES ('c0000008-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000008', 'predisposition', '/ˌpriː.dɪs.pəˈzɪʃ.ən/', 'C2', 2, '[{"order": 1, "pos": "n", "meaning_vi": "khuynh hướng bẩm sinh, thiên hướng sẵn có", "definition_en": "The state of being likely to behave in a particular way or suffer from a particular illness", "example_en": "Researchers discovered that genetic predisposition interacts deeply with environmental stress factors.", "example_vi": "Các nhà nghiên cứu phát hiện rằng thiên hướng di truyền tương tác sâu sắc với các yếu tố căng thẳng môi trường."}]'::jsonb, '["genetic predisposition", "predisposition towards", "psychological predisposition"]'::jsonb, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000008-0000-0000-0000-000000000003', 'c0000008-0000-0000-0000-000000000002', 'sentence_builder', 'khuynh hướng bẩm sinh, thiên hướng', 'Researchers discovered that genetic predisposition interacts deeply with environmental stress factors.', 'Các nhà nghiên cứu phát hiện rằng thiên hướng di truyền tương tác sâu sắc với các yếu tố căng thẳng môi trường.', '["Researchers", "discovered", "that", "genetic", "predisposition", "interacts", "deeply", "with", "environmental", "stress", "factors", "."]'::jsonb, '["aversion", "reluctance"]'::jsonb, 4, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000008-0000-0000-0000-000000000004', 'c0000008-0000-0000-0000-000000000002', 'typing', 'khuynh hướng bẩm sinh, thiên hướng', 'Researchers discovered that genetic ______ interacts deeply with environmental stress factors.', 'Các nhà nghiên cứu phát hiện rằng thiên hướng di truyền tương tác sâu sắc với các yếu tố căng thẳng môi trường.', '["predisposition"]'::jsonb, '["aversion", "reluctance"]'::jsonb, 0, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000008-0000-0000-0000-000000000002', 'tag-c2') ON CONFLICT DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000008-0000-0000-0000-000000000002', 'tag-psychology') ON CONFLICT DO NOTHING;

-- DECK 9: Urbanization, Smart Cities & Architecture
-- Card 8: congestion
INSERT INTO vocab_cards (id, deck_id, term, phonetic, cefr_level, frequency_rank, meanings, collocations, created_at)
VALUES ('c0000009-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000009', 'congestion', '/kənˈdʒes.tʃən/', 'B2', 1, '[{"order": 1, "pos": "n", "meaning_vi": "sự ùn tắc giao thông, tình trạng quá tải không gian", "definition_en": "The situation in which a place is too blocked or crowded, causing difficulties in movement", "example_en": "Municipal authorities invested in rapid transit systems to alleviate persistent traffic congestion.", "example_vi": "Chính quyền thành phố đã đầu tư vào các hệ thống vận tải nhanh để giảm bớt tình trạng ùn tắc giao thông dai dẳng."}]'::jsonb, '["traffic congestion", "ease congestion", "relieve congestion", "congestion charge"]'::jsonb, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000009-0000-0000-0000-000000000001', 'c0000009-0000-0000-0000-000000000001', 'sentence_builder', 'sự ùn tắc giao thông', 'Municipal authorities invested in rapid transit systems to alleviate persistent traffic congestion.', 'Chính quyền thành phố đã đầu tư vào các hệ thống vận tải nhanh để giảm bớt tình trạng ùn tắc giao thông dai dẳng.', '["Municipal", "authorities", "invested", "in", "rapid", "transit", "systems", "to", "alleviate", "persistent", "traffic", "congestion", "."]'::jsonb, '["dispersion", "vacancy"]'::jsonb, 11, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000009-0000-0000-0000-000000000002', 'c0000009-0000-0000-0000-000000000001', 'typing', 'sự ùn tắc giao thông', 'Municipal authorities invested in rapid transit systems to alleviate persistent traffic ______.', 'Chính quyền thành phố đã đầu tư vào các hệ thống vận tải nhanh để giảm bớt tình trạng ùn tắc giao thông dai dẳng.', '["congestion"]'::jsonb, '["dispersion", "vacancy"]'::jsonb, 0, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000009-0000-0000-0000-000000000001', 'tag-b2') ON CONFLICT DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000009-0000-0000-0000-000000000001', 'tag-urban') ON CONFLICT DO NOTHING;

-- Card 9: sustainable
INSERT INTO vocab_cards (id, deck_id, term, phonetic, cefr_level, frequency_rank, meanings, collocations, created_at)
VALUES ('c0000009-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000009', 'sustainable', '/səˈsteɪ.nə.bəl/', 'B2', 2, '[{"order": 1, "pos": "a", "meaning_vi": "bền vững, thân thiện với môi trường và duy trì lâu dài", "definition_en": "Able to continue or be continued for a long time without depleting resources", "example_en": "Urban planners prioritized green building materials to promote sustainable metropolitan architecture.", "example_vi": "Các nhà quy hoạch đô thị ưu tiên vật liệu xây dựng xanh để thúc đẩy kiến trúc đô thị bền vững."}]'::jsonb, '["sustainable development", "sustainable architecture", "sustainable urban mobility"]'::jsonb, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000009-0000-0000-0000-000000000003', 'c0000009-0000-0000-0000-000000000002', 'sentence_builder', 'bền vững, duy trì lâu dài', 'Urban planners prioritized green building materials to promote sustainable metropolitan architecture.', 'Các nhà quy hoạch đô thị ưu tiên vật liệu xây dựng xanh để thúc đẩy kiến trúc đô thị bền vững.', '["Urban", "planners", "prioritized", "green", "building", "materials", "to", "promote", "sustainable", "metropolitan", "architecture", "."]'::jsonb, '["ephemeral", "obsolete"]'::jsonb, 8, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000009-0000-0000-0000-000000000004', 'c0000009-0000-0000-0000-000000000002', 'typing', 'bền vững, duy trì lâu dài', 'Urban planners prioritized green building materials to promote ______ metropolitan architecture.', 'Các nhà quy hoạch đô thị ưu tiên vật liệu xây dựng xanh để thúc đẩy kiến trúc đô thị bền vững.', '["sustainable"]'::jsonb, '["ephemeral", "obsolete"]'::jsonb, 0, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000009-0000-0000-0000-000000000002', 'tag-b2') ON CONFLICT DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000009-0000-0000-0000-000000000002', 'tag-urban') ON CONFLICT DO NOTHING;

-- DECK 10: Education, Pedagogy & Critical Thinking
-- Card 10: pedagogy
INSERT INTO vocab_cards (id, deck_id, term, phonetic, cefr_level, frequency_rank, meanings, collocations, created_at)
VALUES ('c0000010-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000010', 'pedagogy', '/ˈped.ə.ɡɒdʒ.i/', 'C2', 1, '[{"order": 1, "pos": "n", "meaning_vi": "khoa học sư phạm, phương pháp giáo dục và giảng dạy", "definition_en": "The method and practice of teaching, especially as an academic subject or theoretical concept", "example_en": "Modern educational institutions are transforming traditional pedagogy through interactive problem-based learning.", "example_vi": "Các cơ sở giáo dục hiện đại đang chuyển đổi phương pháp sư phạm truyền thống thông qua học tập tương tác dựa trên giải quyết vấn đề."}]'::jsonb, '["innovative pedagogy", "critical pedagogy", "digital pedagogy", "pedagogical approach"]'::jsonb, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000010-0000-0000-0000-000000000001', 'c0000010-0000-0000-0000-000000000001', 'sentence_builder', 'khoa học sư phạm, phương pháp giảng dạy', 'Modern educational institutions are transforming traditional pedagogy through interactive problem-based learning.', 'Các cơ sở giáo dục hiện đại đang chuyển đổi phương pháp sư phạm truyền thống thông qua học tập tương tác dựa trên giải quyết vấn đề.', '["Modern", "educational", "institutions", "are", "transforming", "traditional", "pedagogy", "through", "interactive", "problem-based", "learning", "."]'::jsonb, '["indifference", "dogma"]'::jsonb, 6, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000010-0000-0000-0000-000000000002', 'c0000010-0000-0000-0000-000000000001', 'typing', 'khoa học sư phạm, phương pháp giảng dạy', 'Modern educational institutions are transforming traditional ______ through interactive problem-based learning.', 'Các cơ sở giáo dục hiện đại đang chuyển đổi phương pháp sư phạm truyền thống thông qua học tập tương tác dựa trên giải quyết vấn đề.', '["pedagogy"]'::jsonb, '["indifference", "dogma"]'::jsonb, 0, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000010-0000-0000-0000-000000000001', 'tag-c2') ON CONFLICT DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000010-0000-0000-0000-000000000001', 'tag-education') ON CONFLICT DO NOTHING;

-- Card 11: didactic
INSERT INTO vocab_cards (id, deck_id, term, phonetic, cefr_level, frequency_rank, meanings, collocations, created_at)
VALUES ('c0000010-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000010', 'didactic', '/daɪˈdæk.tɪk/', 'C2', 2, '[{"order": 1, "pos": "a", "meaning_vi": "có tính giáo huấn, mang tính truyền đạt kiến thức chỉ dẫn", "definition_en": "Intended to teach, particularly in having moral instruction as an ulterior motive", "example_en": "The professor abandoned purely didactic lectures in favor of collaborative inquiry and debate.", "example_vi": "Giáo sư đã từ bỏ các bài giảng thuần tính giáo huấn để ủng hộ phương pháp đặt câu hỏi và tranh luận hợp tác."}]'::jsonb, '["didactic approach", "didactic purpose", "didactic instruction"]'::jsonb, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000010-0000-0000-0000-000000000003', 'c0000010-0000-0000-0000-000000000002', 'sentence_builder', 'có tính giáo huấn, truyền đạt', 'The professor abandoned purely didactic lectures in favor of collaborative inquiry and debate.', 'Giáo sư đã từ bỏ các bài giảng thuần tính giáo huấn để ủng hộ phương pháp đặt câu hỏi và tranh luận hợp tác.', '["The", "professor", "abandoned", "purely", "didactic", "lectures", "in", "favor", "of", "collaborative", "inquiry", "and", "debate", "."]'::jsonb, '["frivolous", "erratic"]'::jsonb, 4, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_exercises (id, card_id, exercise_type, meaning_hint, target_sentence, vietnamese_translation, tokens, distractor_tokens, target_index, created_at)
VALUES ('e0000010-0000-0000-0000-000000000004', 'c0000010-0000-0000-0000-000000000002', 'typing', 'có tính giáo huấn, truyền đạt', 'The professor abandoned purely ______ lectures in favor of collaborative inquiry and debate.', 'Giáo sư đã từ bỏ các bài giảng thuần tính giáo huấn để ủng hộ phương pháp đặt câu hỏi và tranh luận hợp tác.', '["didactic"]'::jsonb, '["frivolous", "erratic"]'::jsonb, 0, NOW())
ON CONFLICT (id) DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000010-0000-0000-0000-000000000002', 'tag-c2') ON CONFLICT DO NOTHING;
INSERT INTO card_tags (card_id, tag_id) VALUES ('c0000010-0000-0000-0000-000000000002', 'tag-education') ON CONFLICT DO NOTHING;
