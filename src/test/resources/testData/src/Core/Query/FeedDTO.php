<?php

declare(strict_types=1);

namespace App\Core\Query;

use App\Core\Id\FeedId;

final readonly class FeedDTO
{
    public function __construct(
        public FeedId $id,
        public string $title,
        public string $url,
    ) {
    }
}
