(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('VilleDetailController', VilleDetailController);

    VilleDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Ville'];

    function VilleDetailController($scope, $rootScope, $stateParams, previousState, entity, Ville) {
        var vm = this;

        vm.ville = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:villeUpdate', function(event, result) {
            vm.ville = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
