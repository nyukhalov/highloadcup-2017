ORIGINAL_IMAGE_NAME = com.github.nyukhalov/highloadcup
TARGET_IMAGE_NAME = stor.highloadcup.ru/travels/real_leopard

publish:
	sbt docker && docker tag ${ORIGINAL_IMAGE_NAME} ${TARGET_IMAGE_NAME} && docker push ${TARGET_IMAGE_NAME}
