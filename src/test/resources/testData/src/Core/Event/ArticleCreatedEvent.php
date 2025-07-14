<?php

declare(strict_types=1);

namespace App\Core\Event;

use App\Core\Id\ArticleId;
use App\Infrastructure\Bus\Message\Event\AsyncEventInterface;

final readonly class ArticleCreatedEvent implements AsyncEventInterface
{
    public function __construct(public ArticleId $articleId)
    {
    }
}
