<?php

declare(strict_types=1);

namespace App\Core\Query;

use App\Core\Article\Domain\Entity\Article;
use App\Core\Article\Domain\Entity\ArticleTag;
use App\Core\Article\Domain\Entity\Feed;
use App\Core\Id\ArticleId;
use Brick\DateTime\LocalDateTime;

final readonly class ArticleDTO
{
    public function __construct(
        public ArticleId $id,
        public string $url,
        public string $title,
    ) {
    }
}
