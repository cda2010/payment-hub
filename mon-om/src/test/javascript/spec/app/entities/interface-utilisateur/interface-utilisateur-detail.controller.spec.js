'use strict';

describe('Controller Tests', function() {

    describe('InterfaceUtilisateur Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockInterfaceUtilisateur, MockParametreUtiliateur;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockInterfaceUtilisateur = jasmine.createSpy('MockInterfaceUtilisateur');
            MockParametreUtiliateur = jasmine.createSpy('MockParametreUtiliateur');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'InterfaceUtilisateur': MockInterfaceUtilisateur,
                'ParametreUtiliateur': MockParametreUtiliateur
            };
            createController = function() {
                $injector.get('$controller')("InterfaceUtilisateurDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'omnicanalApp:interfaceUtilisateurUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
