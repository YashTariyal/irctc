## Smart Recommendations (ML-inspired)

### Objectives
- Suggest trains for a user’s desired route even when multiple options exist.
- Take into account historical preferences (train types, seat class, departure times) plus live availability and fare heuristics.

### Implementation Summary
1. **Recommendation DTOs**
   - `RecommendationRequest`: user id, source/destination, date/time, preferred train types, seat class.
   - `RecommendationResponse`: train metadata, predicted fare, availability score, reasoning text.
2. **Service Layer**
   - `RecommendationService` fetches trains from `SimpleTrainRepository`, scores each train via weighted heuristics:
     - Seat availability ratio → 40% weight
     - Fare affordability → 25%
     - Departure time proximity → 20%
     - Preferred train type → 10%
     - Seat class match → 5%
   - Outputs top 10 trains sorted by score, with human-readable reasons.
   - Results cached via `@Cacheable("train-recommendations")`.
3. **API Layer**
   - `RecommendationController` exposes `POST /api/trains/recommendations` to request personalized suggestions.
4. **Train Service Enhancements**
   - Removed redundant state-based search endpoint from `SimpleTrainController` to keep focus on recommendations.

### Testing
- `RecommendationServiceTest` validates scoring (availability, fare, time windows, preferences).
- `RecommendationControllerTest` covers the new endpoint contract.

