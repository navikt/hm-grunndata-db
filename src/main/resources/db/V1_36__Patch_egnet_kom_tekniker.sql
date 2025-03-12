UPDATE product_v1
SET attributes = jsonb_set(
        attributes,
        '{egnetForKommunalTekniker}',
        'true'::jsonb
                 )
WHERE jsonb_array_length(attributes->'compatibleWith'->'productIds') > 0;