<?php

declare(strict_types=1);

namespace App\UserInterface\Web\Api\Article\Get;

use App\UserInterface\Web\Api\Article\Models\Article;
use OpenApi\Attributes as OA;

#[OA\Schema(
    required: ['article'],
)]
final readonly class Response
{
    public function __construct(
        public Article $article,
    ) {
    }
}
