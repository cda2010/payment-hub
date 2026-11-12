(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ViewFileController', ViewFileController);

    ViewFileController.$inject = ['$state','$uibModalInstance', 'Document','ParseLinks', 'AlertService','entity'];

    function ViewFileController($state,$uibModalInstance,Document ,ParseLinks, AlertService, entity) {

        var vm = this;
        vm.clear = clear;
        vm.entity=entity;
        console.log('log',vm.entity);
        //vm.src='data:' +vm.entity.type + ';base64,'+escape(vm.entity.valeur);
        //vm.imgSrc='pti/src/main/resources/images/Photo_identit.jpeg';
        vm.imageDTO= {
            documentId: vm.entity.documentId,
            valeurContentType: null
        };
        console.log(vm.imageDTO);
        if(vm.entity.imageFolder===null|| vm.entity.imageFolder===undefined){
            console.log('ok----');
            vm.imgSrc='data:' +vm.entity.type + ';base64,'+escape(vm.entity.valeur);
        } else {
            Document.getImage({documentId:vm.imageDTO.documentId},function (result) {
                //vm.imgSrc='data:'+';base64,'+escape(result.imageFinal);
                vm.imgSrc=result.imageLink;
            });
        }

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }
    }
})();
