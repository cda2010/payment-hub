(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CompteAutreDetailController', CompteAutreDetailController);

    CompteAutreDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'CompteAutre'];

    function CompteAutreDetailController($scope, $rootScope, $stateParams, previousState, entity, CompteAutre) {
        var vm = this;

        vm.compteAutre = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:compteAutreUpdate', function(event, result) {
            vm.compteAutre = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
