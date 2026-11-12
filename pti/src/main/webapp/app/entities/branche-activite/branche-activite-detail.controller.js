(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BrancheActiviteDetailController', BrancheActiviteDetailController);

    BrancheActiviteDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'BrancheActivite'];

    function BrancheActiviteDetailController($scope, $rootScope, $stateParams, previousState, entity, BrancheActivite) {
        var vm = this;

        vm.brancheActivite = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:brancheActiviteUpdate', function(event, result) {
            vm.brancheActivite = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
