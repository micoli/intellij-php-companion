<?php

declare(strict_types=1);

namespace App\Core\Query\Feed;

use App\Core\Query\FeedDTO;
use App\Infrastructure\Bus\Message\Query\SyncQueryResultInterface;

final readonly class Result implements SyncQueryResultInterface
{
    /**
     * @param list<FeedDTO> $feedDTOs
     */
    public function __construct(
        public array $feedDTOs,
    ) {
    }
}
