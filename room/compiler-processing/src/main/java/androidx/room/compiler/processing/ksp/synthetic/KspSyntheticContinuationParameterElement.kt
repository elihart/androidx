/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.room.compiler.processing.ksp.synthetic

import androidx.room.compiler.processing.XAnnotationBox
import androidx.room.compiler.processing.XDeclaredType
import androidx.room.compiler.processing.XEquality
import androidx.room.compiler.processing.XExecutableParameterElement
import androidx.room.compiler.processing.XType
import androidx.room.compiler.processing.ksp.KspExecutableElement
import androidx.room.compiler.processing.ksp.KspProcessingEnv
import androidx.room.compiler.processing.ksp.KspType
import androidx.room.compiler.processing.ksp.requireContinuationClass
import androidx.room.compiler.processing.ksp.returnTypeAsMemberOf
import androidx.room.compiler.processing.ksp.swapResolvedType
import org.jetbrains.kotlin.ksp.symbol.Variance
import kotlin.reflect.KClass

/**
 * XProcessing adds an additional argument to each suspend function for the continiuation because
 * this is what KAPT generates and Room needs it as long as it generates java code.
 */
internal class KspSyntheticContinuationParameterElement(
    private val env: KspProcessingEnv,
    private val containing: KspExecutableElement
) : XExecutableParameterElement, XEquality {

    override val name: String
        get() = "_syntheticContinuation"

    override val equalityItems: Array<out Any?> by lazy {
        arrayOf("continuation", containing)
    }

    override val type: XType by lazy {
        val continuation = env.resolver.requireContinuationClass()
        val contType = continuation.asType(
            listOf(
                env.resolver.getTypeArgument(
                    checkNotNull(containing.declaration.returnType) {
                        "cannot find return type for $this"
                    },
                    Variance.CONTRAVARIANT
                )
            )
        )
        env.wrap(contType)
    }

    override fun asMemberOf(other: XDeclaredType): XType {
        check(other is KspType)
        val continuation = env.resolver.requireContinuationClass()
        val asMember = containing.declaration.returnTypeAsMemberOf(
            resolver = env.resolver,
            ksType = other.ksType
        )
        val returnTypeRef = checkNotNull(containing.declaration.returnType) {
            "cannot find return type reference for $this"
        }
        val returnTypeAsTypeArgument = env.resolver.getTypeArgument(
            returnTypeRef.swapResolvedType(asMember),
            Variance.CONTRAVARIANT
        )
        val contType = continuation.asType(listOf(returnTypeAsTypeArgument))
        return env.wrap(contType)
    }

    override fun kindName(): String {
        return "synthetic continuation parameter"
    }

    override fun <T : Annotation> toAnnotationBox(annotation: KClass<T>): XAnnotationBox<T>? {
        TODO("Not yet implemented")
    }

    override fun hasAnnotationWithPackage(pkg: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasAnnotation(annotation: KClass<out Annotation>): Boolean {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        return XEquality.equals(this, other)
    }

    override fun hashCode(): Int {
        return XEquality.hashCode(equalityItems)
    }
}
