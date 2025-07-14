<?php

declare(strict_types=1);

namespace App\Core\Event;

use App\Core\Id\FeedId;
use App\Infrastructure\Bus\Message\Event\AsyncEventInterface;

final readonly class FeedCreatedEvent implements AsyncEventInterface
{
    public function __construct(public FeedId $feedId)
    {
    }
}
