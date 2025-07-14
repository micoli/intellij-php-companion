<?php

namespace App\Tests\Func\Models;

use Cake\Chronos\Chronos;
use Ramsey\Uuid\UuidInterface;

final readonly class AnObject
{
    protected Chronos $updatedAt;

    public function __construct(
        private UuidInterface $id,
        private string        $name,
        private ?string       $label,
        public Chronos        $createdAt,
        public AnInnerObject  $innerObject,
    )
    {
        $this->updatedAt = Chronos::now();
    }

    public function getId(): UuidInterface
    {
        return $this->id;
    }

    public function getName(): string
    {
        return $this->name;
    }

    public function getLabel(): ?string
    {
        return $this->label;
    }

    public function getCreatedAt(): Chronos
    {
        return $this->createdAt;
    }

    public function getUpdatedAt(): Chronos
    {
        return $this->updatedAt;
    }

    public function getAnInnerObject(): AnInnerObject
    {
        return $this->innerObject;
    }
}