package com.anantakala.backend.dto.category;

import tools.jackson.databind.JsonNode;

public record CategoryPatchRequest(
        JsonNode name,
        JsonNode slug,
        JsonNode description,
        JsonNode parentId,
        JsonNode active
) {
}

//Why use JsonNode for PATCH?
//
//Because we need to distinguish three situations:
//JSON request	                Meaning
//{}	                        Field not supplied — leave unchanged
//{"description": null}	        Field supplied as null — clear description
//{"description": "New text"}	Field supplied with a value — update it
//
//With a regular String description, Jackson typically maps both missing and explicit null
// to Java null. We would lose that distinction.
//
//With JsonNode:
//- Missing field → Java null
//- Explicit JSON null → NullNode
//- Supplied value → appropriate JSON node
//For our PATCH implementation, we'll support clearing nullable fields (description and parentId)
// while rejecting null for required fields.
