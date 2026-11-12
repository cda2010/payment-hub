(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LienViewController', LienViewController);

    LienViewController.$inject = ['$scope', '$rootScope',  'entity', '$uibModalInstance','Lien', 'Personne'];

    function LienViewController($scope, $rootScope,entity, $uibModalInstance,Lien, Personne) {
        var vm = this;

        vm.lien = entity;
        Lien.getLien({lienId:vm.lien},function (result) {
            console.log(result);
            vm.result=result;
        });
        vm.clear = function () {
            $uibModalInstance.dismiss('cancel');
        }

    }
})();
