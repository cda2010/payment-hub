(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ReclamationDetailController', ReclamationDetailController);

    ReclamationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity',];

    function ReclamationDetailController($scope, $rootScope, $stateParams, previousState, entity,) {
        var vm = this;

        vm.reclamation= entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:reclamationUpdate', function(event, result) {
            vm.reclamation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
