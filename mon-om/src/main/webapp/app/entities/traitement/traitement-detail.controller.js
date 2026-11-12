(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('TraitementDetailController', TraitementDetailController);

    TraitementDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Traitement'];

    function TraitementDetailController($scope, $rootScope, $stateParams, previousState, entity, Traitement) {
        var vm = this;

        vm.traitement = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalApp:traitementUpdate', function(event, result) {
            vm.traitement = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
