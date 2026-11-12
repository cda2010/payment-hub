(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LienDetailController', LienDetailController);

    LienDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Lien', 'Personne'];

    function LienDetailController($scope, $rootScope, $stateParams, previousState, entity, Lien, Personne) {
        var vm = this;

        vm.lien = entity;
        vm.previousState = previousState.name;
        Lien.getLien({lienId:vm.lien},function (result) {
            console.log(result);
            vm.result=result;
        });
        var unsubscribe = $rootScope.$on('ptiApp:lienUpdate', function(event, result) {
            vm.lien = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
